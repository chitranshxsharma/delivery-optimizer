// ================================================================
//  DELIVERY OPTIMIZER — Global JS
//  Handles: JWT token storage, API calls, UI helpers
// ================================================================

const API = {
  base: '/api',

  getToken() { return localStorage.getItem('jwt_token'); },
  setToken(t) { localStorage.setItem('jwt_token', t); },
  clearToken() { localStorage.removeItem('jwt_token'); localStorage.removeItem('user_info'); },
  getUser()  { try { return JSON.parse(localStorage.getItem('user_info') || '{}'); } catch { return {}; } },
  setUser(u) { localStorage.setItem('user_info', JSON.stringify(u)); },

  headers() {
    const h = { 'Content-Type': 'application/json' };
    const t = this.getToken();
    if (t) h['Authorization'] = 'Bearer ' + t;
    return h;
  },

  async post(path, body) {
    const r = await fetch(this.base + path, { method: 'POST', headers: this.headers(), body: JSON.stringify(body) });
    return r.json();
  },

  async get(path) {
    const r = await fetch(this.base + path, { headers: this.headers() });
    return r.json();
  },

  async patch(path, body) {
    const r = await fetch(this.base + path, { method: 'PATCH', headers: this.headers(), body: JSON.stringify(body) });
    return r.json();
  },

  async delete(path) {
    const r = await fetch(this.base + path, { method: 'DELETE', headers: this.headers() });
    return r.json();
  }
};

// ── Toast notifications ───────────────────────────────────────
function showToast(msg, type = 'success') {
  const existing = document.querySelector('.toast-container');
  if (!existing) {
    const c = document.createElement('div');
    c.className = 'toast-container';
    c.style.cssText = 'position:fixed;top:1.5rem;right:1.5rem;z-index:9999;display:flex;flex-direction:column;gap:8px;';
    document.body.appendChild(c);
  }
  const container = document.querySelector('.toast-container');
  const toast = document.createElement('div');
  const colors = { success: '#22C55E', danger: '#EF4444', info: '#14B8A6', warn: '#EAB308' };
  toast.style.cssText = `
    background:#111827; border:1px solid rgba(255,255,255,0.1);
    border-left:3px solid ${colors[type] || colors.success};
    color:#F1F5F9; padding:0.75rem 1rem; border-radius:8px;
    font-size:0.85rem; max-width:320px; font-family:'DM Sans',sans-serif;
    animation:fadeIn 0.2s ease; box-shadow:0 4px 24px rgba(0,0,0,0.4);
  `;
  toast.textContent = msg;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3500);
}

// ── Badge helper ──────────────────────────────────────────────
function statusBadge(status) {
  const s = (status || '').toLowerCase();
  const labels = { pending:'⏳ Pending', assigned:'👤 Assigned', dispatched:'🚛 Dispatched',
                   in_transit:'📦 In Transit', delivered:'✅ Delivered', failed:'❌ Failed' };
  return `<span class="badge badge-${s}">${labels[s] || status}</span>`;
}

function roleBadge(role) {
  const r = (role || '').toLowerCase().replace('role_','');
  return `<span class="badge badge-${r}">${r.charAt(0).toUpperCase()+r.slice(1)}</span>`;
}

// ── Route path renderer ───────────────────────────────────────
function renderRoutePath(pathStr) {
  if (!pathStr) return '<span class="text-muted">—</span>';
  const nodes = pathStr.split(' → ');
  return '<div class="route-path">' + nodes.map((n, i) =>
    `<span class="route-node">${n}</span>` +
    (i < nodes.length - 1 ? '<span class="route-arrow">→</span>' : '')
  ).join('') + '</div>';
}

// ── Auth guard — redirect to login if no token ────────────────
function requireAuth() {
  if (!API.getToken()) { window.location.href = '/login'; return false; }
  return true;
}

// ── Logout ────────────────────────────────────────────────────
function logout() {
  API.clearToken();
  window.location.href = '/login';
}

// ── Populate user info in sidebar ────────────────────────────
function populateSidebarUser() {
  const u = API.getUser();
  const nameEl = document.getElementById('sidebar-user-name');
  const roleEl = document.getElementById('sidebar-user-role');
  if (nameEl) nameEl.textContent = u.name || 'User';
  if (roleEl) roleEl.textContent = (u.role || '').replace('ROLE_', '');
}

// ── Highlight active nav link ─────────────────────────────────
function setActiveNav() {
  const path = window.location.pathname;
  document.querySelectorAll('.nav-link').forEach(a => {
    a.classList.toggle('active', a.getAttribute('href') === path);
  });
}

document.addEventListener('DOMContentLoaded', () => {
  populateSidebarUser();
  setActiveNav();
});