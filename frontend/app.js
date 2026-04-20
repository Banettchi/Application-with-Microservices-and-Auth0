/* ─────────────────────────────────────────────────────────────────────────────
   Twitly — Frontend Application
   Uses Auth0 SPA JS SDK (loaded via CDN) + Fetch API to talk to the backend.

   ⚠️  CONFIGURATION:
       Edit the CONFIG object below with your Auth0 credentials before running.
   ───────────────────────────────────────────────────────────────────────────── */

const CONFIG = {
  // ── Auth0 Settings (replace with your own values) ──────────────────────────
  auth0Domain:   'YOUR_DOMAIN.us.auth0.com',   // e.g. dev-abc123.us.auth0.com
  auth0ClientId: 'YOUR_CLIENT_ID',             // SPA Application Client ID
  auth0Audience: 'https://twitter-app-api',    // API Audience defined in Auth0

  // ── Backend API ─────────────────────────────────────────────────────────────
  apiBaseUrl: 'http://localhost:8080',

  // ── Stream polling interval (ms) ────────────────────────────────────────────
  pollInterval: 15000,
};

/* ─── State ────────────────────────────────────────────────────────────────── */
let auth0Client = null;
let pollTimer   = null;

/* ─── DOM References ───────────────────────────────────────────────────────── */
const $ = id => document.getElementById(id);

const btnLogin     = $('btn-login');
const btnLoginCta  = $('btn-login-cta');
const btnLogout    = $('btn-logout');
const btnPost      = $('btn-post');
const btnRefresh   = $('btn-refresh');
const userMenu     = $('user-menu');
const userAvatar   = $('user-avatar');
const composerAvatar = $('composer-avatar');
const userName     = $('user-name');
const loginCta     = $('login-cta');
const composerSection = $('composer-section');
const postInput    = $('post-input');
const charCount    = $('char-count');
const streamEl     = $('stream');
const streamLoading = $('stream-loading');
const streamEmpty  = $('stream-empty');

/* ═══════════════════════════════════════════════════════════════════════════
   INITIALIZATION
   ═══════════════════════════════════════════════════════════════════════════ */

async function init() {
  try {
    // Initialize Auth0 client
    auth0Client = await auth0.createAuth0Client({
      domain:        CONFIG.auth0Domain,
      clientId:      CONFIG.auth0ClientId,
      authorizationParams: {
        audience:    CONFIG.auth0Audience,
        scope:       'openid profile email read:posts write:posts read:profile',
      },
      cacheLocation: 'localstorage',
      useRefreshTokens: true,
    });

    // Handle redirect callback after Auth0 login
    const query = window.location.search;
    if (query.includes('code=') && query.includes('state=')) {
      await auth0Client.handleRedirectCallback();
      // Clean URL after callback
      window.history.replaceState({}, document.title, window.location.pathname);
    }

    await updateUI();
    await loadStream();
    startPolling();

  } catch (err) {
    console.error('Auth0 initialization error:', err);
    showToast('Failed to initialize authentication. Check your Auth0 configuration.', 'error');
    // Still load the stream (public endpoint)
    await loadStream();
  }
}

/* ═══════════════════════════════════════════════════════════════════════════
   AUTH HELPERS
   ═══════════════════════════════════════════════════════════════════════════ */

async function login() {
  try {
    await auth0Client.loginWithRedirect({
      authorizationParams: {
        redirect_uri: window.location.origin + window.location.pathname,
      },
    });
  } catch (err) {
    console.error('Login error:', err);
    showToast('Login failed. Please try again.', 'error');
  }
}

async function logout() {
  try {
    await auth0Client.logout({
      logoutParams: {
        returnTo: window.location.origin + window.location.pathname,
      },
    });
  } catch (err) {
    console.error('Logout error:', err);
    showToast('Logout failed. Please try again.', 'error');
  }
}

async function getAccessToken() {
  try {
    return await auth0Client.getTokenSilently();
  } catch (err) {
    console.warn('Could not get access token silently:', err);
    return null;
  }
}

/* ═══════════════════════════════════════════════════════════════════════════
   UI STATE MANAGEMENT
   ═══════════════════════════════════════════════════════════════════════════ */

async function updateUI() {
  if (!auth0Client) return;

  const isAuthenticated = await auth0Client.isAuthenticated();

  if (isAuthenticated) {
    const user = await auth0Client.getUser();

    // Show user menu, hide login button
    btnLogin.classList.add('hidden');
    userMenu.classList.remove('hidden');

    // Populate user info
    if (user.picture) {
      userAvatar.src = user.picture;
      userAvatar.alt = user.name || 'User avatar';
      composerAvatar.src = user.picture;
      composerAvatar.alt = user.name || 'User avatar';
    } else {
      // Replace img with placeholder initials
      const initials = getInitials(user.name || user.email || 'U');
      replaceAvatarWithPlaceholder(userAvatar, initials, 'avatar');
      replaceAvatarWithPlaceholder(composerAvatar, initials, 'avatar avatar-lg');
    }

    userName.textContent = user.name || user.email || 'User';

    // Show composer, hide CTA
    composerSection.classList.remove('hidden');
    loginCta.classList.add('hidden');

  } else {
    // Show login button, hide user menu & composer
    btnLogin.classList.remove('hidden');
    userMenu.classList.add('hidden');
    composerSection.classList.add('hidden');
    loginCta.classList.remove('hidden');
  }
}

function replaceAvatarWithPlaceholder(imgEl, initials, classes) {
  const div = document.createElement('div');
  div.className = 'avatar-placeholder ' + classes;
  div.textContent = initials;
  imgEl.replaceWith(div);
}

function getInitials(name) {
  return name
    .split(' ')
    .map(w => w[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();
}

/* ═══════════════════════════════════════════════════════════════════════════
   API CALLS
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Generic authenticated fetch wrapper.
 * Automatically attaches Bearer token when available.
 */
async function apiFetch(path, options = {}) {
  const url = CONFIG.apiBaseUrl + path;
  const headers = { 'Content-Type': 'application/json', ...options.headers };

  const token = await getAccessToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(url, { ...options, headers });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`API error ${response.status}: ${errorText}`);
  }

  // Return null for 204 No Content
  if (response.status === 204) return null;
  return response.json();
}

/**
 * Load the public stream from GET /api/stream
 */
async function loadStream() {
  streamLoading.classList.remove('hidden');
  streamEmpty.classList.add('hidden');

  try {
    const data = await apiFetch('/api/stream');
    renderStream(data.posts || []);
  } catch (err) {
    console.error('Failed to load stream:', err);
    // Try fallback to /api/posts
    try {
      const posts = await apiFetch('/api/posts');
      renderStream(Array.isArray(posts) ? posts : []);
    } catch {
      showToast('Could not load posts. Is the backend running?', 'error');
      streamLoading.classList.add('hidden');
    }
  }
}

/**
 * Create a new post via POST /api/posts (requires JWT)
 */
async function createPost(content) {
  return apiFetch('/api/posts', {
    method: 'POST',
    body: JSON.stringify({ content }),
  });
}

/**
 * Get current user info from GET /api/me (requires JWT)
 */
async function getMe() {
  return apiFetch('/api/me');
}

/* ═══════════════════════════════════════════════════════════════════════════
   STREAM RENDERING
   ═══════════════════════════════════════════════════════════════════════════ */

function renderStream(posts) {
  streamLoading.classList.add('hidden');
  streamEl.innerHTML = '';

  if (!posts || posts.length === 0) {
    streamEmpty.classList.remove('hidden');
    return;
  }

  streamEmpty.classList.add('hidden');

  posts.forEach(post => {
    const card = createPostCard(post);
    streamEl.appendChild(card);
  });
}

function createPostCard(post) {
  const card = document.createElement('article');
  card.className = 'post-card';
  card.setAttribute('aria-label', `Post by ${post.authorName}`);

  const initials = getInitials(post.authorName || 'U');
  const timeAgo  = formatTimeAgo(post.createdAt);

  card.innerHTML = `
    <div class="avatar-placeholder avatar-post" aria-hidden="true">${escapeHtml(initials)}</div>
    <div class="post-body">
      <div class="post-meta">
        <span class="post-author">${escapeHtml(post.authorName || 'Unknown')}</span>
        <span class="post-dot" aria-hidden="true">●</span>
        <time class="post-time" datetime="${post.createdAt}" title="${new Date(post.createdAt).toLocaleString()}">${timeAgo}</time>
      </div>
      <p class="post-content">${escapeHtml(post.content)}</p>
    </div>
  `;

  return card;
}

/* ═══════════════════════════════════════════════════════════════════════════
   EVENT HANDLERS
   ═══════════════════════════════════════════════════════════════════════════ */

// Login buttons
btnLogin.addEventListener('click', login);
btnLoginCta.addEventListener('click', login);

// Logout button
btnLogout.addEventListener('click', logout);

// Refresh stream
btnRefresh.addEventListener('click', async () => {
  btnRefresh.disabled = true;
  btnRefresh.textContent = '↻ ...';
  await loadStream();
  btnRefresh.disabled = false;
  btnRefresh.textContent = '↻ Refresh';
});

// Character counter
postInput.addEventListener('input', () => {
  const remaining = 140 - postInput.value.length;
  charCount.textContent = remaining;

  charCount.classList.remove('warning', 'danger');
  if (remaining <= 0)  charCount.classList.add('danger');
  else if (remaining <= 20) charCount.classList.add('warning');
});

// Submit post with Enter+Ctrl or button
postInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
    e.preventDefault();
    handlePostSubmit();
  }
});

btnPost.addEventListener('click', handlePostSubmit);

async function handlePostSubmit() {
  const content = postInput.value.trim();

  if (!content) {
    showToast('Post cannot be empty.', 'error');
    return;
  }

  if (content.length > 140) {
    showToast('Post must be 140 characters or less.', 'error');
    return;
  }

  btnPost.disabled = true;
  btnPost.textContent = 'Posting...';

  try {
    const newPost = await createPost(content);

    postInput.value = '';
    charCount.textContent = '140';
    charCount.classList.remove('warning', 'danger');

    // Prepend the new post to the stream immediately
    prependPost(newPost);
    showToast('Posted! ✨', 'success');

  } catch (err) {
    console.error('Failed to create post:', err);
    if (err.message.includes('401')) {
      showToast('Session expired. Please log in again.', 'error');
    } else {
      showToast('Failed to post. Please try again.', 'error');
    }
  } finally {
    btnPost.disabled = false;
    btnPost.textContent = 'Post';
  }
}

function prependPost(post) {
  streamEmpty.classList.add('hidden');
  const card = createPostCard(post);

  if (streamEl.firstChild) {
    streamEl.insertBefore(card, streamEl.firstChild);
    // Re-apply border radius
    updateStreamCardBorders();
  } else {
    streamEl.appendChild(card);
  }
}

function updateStreamCardBorders() {
  const cards = streamEl.querySelectorAll('.post-card');
  cards.forEach((card, i) => {
    card.classList.remove('first-card', 'last-card');
  });
}

/* ═══════════════════════════════════════════════════════════════════════════
   POLLING
   ═══════════════════════════════════════════════════════════════════════════ */

function startPolling() {
  if (pollTimer) clearInterval(pollTimer);
  pollTimer = setInterval(loadStream, CONFIG.pollInterval);
}

/* ═══════════════════════════════════════════════════════════════════════════
   UTILITIES
   ═══════════════════════════════════════════════════════════════════════════ */

/**
 * Escape HTML to prevent XSS in user-generated content.
 */
function escapeHtml(str) {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

/**
 * Format a timestamp as a relative "time ago" string.
 */
function formatTimeAgo(isoString) {
  if (!isoString) return '';
  const now  = Date.now();
  const then = new Date(isoString).getTime();
  const diff = Math.floor((now - then) / 1000); // seconds

  if (diff < 60)   return 'just now';
  if (diff < 3600) return `${Math.floor(diff / 60)}m`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h`;
  return `${Math.floor(diff / 86400)}d`;
}

/**
 * Show a toast notification.
 * @param {string} message - The message to display
 * @param {'success'|'error'|''} type - Toast style
 */
function showToast(message, type = '') {
  const toast = $('toast');
  toast.textContent = message;
  toast.className = `toast ${type} visible`;

  setTimeout(() => {
    toast.classList.remove('visible');
  }, 3500);
}

/* ═══════════════════════════════════════════════════════════════════════════
   BOOTSTRAP
   ═══════════════════════════════════════════════════════════════════════════ */
window.addEventListener('DOMContentLoaded', init);
