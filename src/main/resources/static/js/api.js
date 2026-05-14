// ── NexusA API Layer ──────────────────────────────────────────────────────────
const API_BASE = 'http://localhost:8080';
const TOKEN_KEY = 'nexusa_token';

// ── Token Helpers ─────────────────────────────────────────────────────────────
const Auth = {
    getToken() { return localStorage.getItem(TOKEN_KEY); },
    setToken(token) { localStorage.setItem(TOKEN_KEY, token); },
    removeToken() { localStorage.removeItem(TOKEN_KEY); },

    getPayload() {
        const token = this.getToken();
        if (!token) return null;
        try {
            return JSON.parse(atob(token.split('.')[1]));
        } catch { return null; }
    },

    getRole() {
        const payload = this.getPayload();
        return payload?.role || null;
    },

    isAdmin() { return this.getRole() === 'ADMIN'; },
    isEditor() { return this.getRole() === 'EDITOR'; },
    isViewer() { return this.getRole() === 'VIEWER'; },

    getUserId() {
        const payload = this.getPayload();
        return payload?.sub || payload?.userId || payload?.id || null;
    },

    requireAuth(redirectTo = 'login.html') {
        if (!this.getToken()) {
            window.location.href = redirectTo;
            return false;
        }
        return true;
    },

    logout() {
        this.removeToken();
        window.location.href = 'login.html';
    }
};

// ── Core Fetch ────────────────────────────────────────────────────────────────
async function apiFetch(path, options = {}) {
    const token = Auth.getToken();
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const res = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers,
        body: options.body ? JSON.stringify(options.body) : undefined
    });

    const text = await res.text();
    let data;
    try { data = JSON.parse(text); } catch { data = text; }

    if (!res.ok) {
        const msg = typeof data === 'string' ? data : (data?.message || `Request failed (${res.status})`);
        throw new Error(msg);
    }
    return data;
}

// ── Auth API ──────────────────────────────────────────────────────────────────
const AuthAPI = {
    async login(email, password) {
        const token = await apiFetch('/auth/login', {
            method: 'POST',
            body: { email, password }
        });
        Auth.setToken(token);
        return token;
    },

    async register(payload) {
        const token = await apiFetch('/auth/register', {
            method: 'POST',
            body: payload
        });
        Auth.setToken(token);
        return token;
    },

    async getUniversities() {
        return apiFetch('/auth/universities');
    }
};

// ── Civilization API ──────────────────────────────────────────────────────────
const CivAPI = {
    async create(data) {
        return apiFetch('/civilization/create', { method: 'POST', body: data });
    },

    async getMy() {
        return apiFetch('/civilization/my');
    },

    async getAll() {
        return apiFetch('/civilization/all');
    },

    async getLatest(civId) {
        return apiFetch(`/civilization/${civId}/latest`);
    },

    async getVersions(civId) {
        return apiFetch(`/civilization/${civId}/versions`);
    },

    async addVolume(civId, data) {
        return apiFetch(`/civilization/${civId}/volume`, { method: 'POST', body: data });
    },

    async addEntry(civId, data) {
        return apiFetch(`/civilization/${civId}/entry`, { method: 'POST', body: data });
    },

    async updateNode(civId, nodeId, data) {
        return apiFetch(`/civilization/${civId}/node/${nodeId}`, { method: 'PUT', body: data });
    },

    async rollback(civId, hash) {
        return apiFetch(`/civilization/${civId}/rollback`, { method: 'POST', body: { hash } });
    },

    async assignEditor(civId, userId) {
        return apiFetch(`/civilization/${civId}/editors`, { method: 'POST', body: { userId } });
    },

    async getEditors(civId) {
        return apiFetch(`/civilization/${civId}/editors`);
    },

    async getUniversityUsers() {
        return apiFetch('/civilization/users');
    }
};

// ── Toast System ──────────────────────────────────────────────────────────────
function initToasts() {
    if (!document.getElementById('toast-container')) {
        const el = document.createElement('div');
        el.id = 'toast-container';
        el.className = 'toast-container';
        document.body.appendChild(el);
    }
}

function showToast(message, type = 'info', duration = 3500) {
    initToasts();
    const container = document.getElementById('toast-container');
    const icons = { success: '✓', error: '✕', info: 'ℹ' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${icons[type]}</span><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(20px)';
        toast.style.transition = '0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

// ── Modal Helpers ─────────────────────────────────────────────────────────────
function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('active');
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('active');
}

// Close on overlay click
document.addEventListener('click', e => {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('active');
    }
});

// ── University Dropdown Loader ─────────────────────────────────────────────────
async function loadUniversities(selectId, selectedId = null) {
    const select = document.getElementById(selectId);
    if (!select) return;
    select.innerHTML = '<option value="">Loading universities…</option>';
    try {
        const unis = await AuthAPI.getUniversities();
        select.innerHTML = '<option value="">Select your university</option>';
        unis.forEach(u => {
            const opt = document.createElement('option');
            opt.value = u.id;
            opt.textContent = u.name;
            if (selectedId && u.id === selectedId) opt.selected = true;
            select.appendChild(opt);
        });
    } catch (err) {
        select.innerHTML = '<option value="">Failed to load universities</option>';
        showToast('Could not load universities', 'error');
    }
}

// ── Role Badge ────────────────────────────────────────────────────────────────
function roleBadge(role) {
    const classes = { ADMIN: 'badge-admin', EDITOR: 'badge-editor', VIEWER: 'badge-viewer' };
    return `<span class="badge ${classes[role] || 'badge-viewer'}">${role}</span>`;
}

// ── Format year (handles negatives) ──────────────────────────────────────────
function formatYear(year) {
    if (year == null) return '—';
    if (year < 0) return `${Math.abs(year)} BCE`;
    return `${year} CE`;
}

// ── Truncate hash ─────────────────────────────────────────────────────────────
function shortHash(hash) {
    return hash ? hash.substring(0, 8) : '';
}