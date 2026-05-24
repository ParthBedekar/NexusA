Auth.requireAuth();

const role    = Auth.getRole();
const payload = Auth.getPayload();

/* ─── Init UI ─── */
function initUI() {
    const name   = payload?.sub || 'User';
    const letter = name.charAt(0).toUpperCase();

    document.getElementById('user-avatar').textContent    = letter;
    document.getElementById('user-name').textContent      = name;
    document.getElementById('user-role-label').textContent = role || '—';
    document.getElementById('nav-role').innerHTML =
        `<span class="role-badge role-${(role||'viewer').toLowerCase()}">${role}</span>`;

    if (role === 'ADMIN') {
        document.getElementById('page-title').textContent = 'University Civilizations';
        document.getElementById('page-sub').textContent   = 'All civilizations managed by your institution';

        document.getElementById('users-sidebar-btn').style.display = 'flex';
        document.getElementById('users-tab-btn').style.display     = 'inline-block';

        document.getElementById('header-actions').innerHTML =
            `<button class="btn-new" onclick="openModal('modal-create-civ')">+ New Civilization</button>`;
    } else if (role === 'EDITOR') {
        document.getElementById('page-title').textContent = 'My Civilizations';
        document.getElementById('page-sub').textContent   = 'Civilization trees you have been assigned to edit';
    } else {
        document.getElementById('page-title').textContent = 'Civilizations';
        document.getElementById('page-sub').textContent   = 'Browse civilizations from your institution';
    }
}

/* ─── Tab switching ─── */
function switchTab(name, clickedBtn) {
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.tab-btn, .sidebar-link').forEach(b => b.classList.remove('active'));

    document.getElementById(`tab-${name}`).classList.add('active');
    clickedBtn.classList.add('active');

    /* keep both tab-nav btn and sidebar-link in sync */
    if (name === 'users') {
        document.getElementById('users-tab-btn').classList.add('active');
        document.getElementById('users-sidebar-btn').classList.add('active');
        loadUsers();
    } else {
        document.querySelectorAll('.tab-btn')[0].classList.add('active');
        document.querySelectorAll('.sidebar-link')[0].classList.add('active');
    }
}

/* ─── Load civilizations ─── */
async function loadCivilizations() {
    const wrap = document.getElementById('civ-wrap');
    try {
        const civs = role === 'ADMIN' ? await CivAPI.getAll() : await CivAPI.getMy();

        if (!civs || !civs.length) {
            wrap.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-icon">◈</div>
                        <h3>${role === 'ADMIN' ? 'No civilizations yet' : 'No civilizations assigned'}</h3>
                        <p>${role === 'ADMIN'
                ? 'Create your first civilization tree to get started.'
                : 'Contact your institution admin to be assigned as an editor.'}</p>
                    </div>`;
            return;
        }

        wrap.innerHTML = `<div class="civ-grid">${civs.map(civCard).join('')}</div>`;
    } catch (err) {
        wrap.innerHTML = `<div class="empty-state"><h3>Failed to load</h3><p>${err.message}</p></div>`;
    }
}

function civCard(c) {
    const start = formatYear(c.startDate ?? c.startYear);
    const end   = formatYear(c.endDate ?? c.endYear);

    const deleteBtn = role === 'ADMIN'
        ? `<button class="btn-delete-civ" data-id="${c.civId}" onclick="deleteCivilization(event, '${c.civId}')">Delete</button>`
        : '';

    return `
        <a class="civ-card" href="civilization.html?civId=${c.civId}">
            <div class="civ-card-top">
                <div class="civ-title">${c.title || 'Untitled'}</div>
                <div class="civ-years">${start} – ${end}</div>
            </div>
            <p class="civ-desc">${c.description || 'No description provided.'}</p>
            <div class="civ-card-footer">
                <span class="link-btn">Open →</span>
                ${deleteBtn}
            </div>
        </a>`;
}
async function deleteCivilization(event, civId) {
    event.preventDefault();   // stop the <a> from navigating
    event.stopPropagation();

    if (!confirm('Permanently delete this civilization and all its versions?')) return;

    try {
        await CivAPI.delete(civId);
        showToast('Civilization deleted', 'success');
        loadCivilizations();
    } catch (err) {
        showToast(err.message || 'Delete failed', 'error');
    }
}
/* ─── Load users (Admin) ─── */
let usersLoaded = false;
async function loadUsers() {
    if (usersLoaded) return;
    const wrap = document.getElementById('users-wrap');
    try {
        const users = await CivAPI.getUniversityUsers();
        if (!users || !users.length) {
            wrap.innerHTML = `<div class="empty-state"><h3>No users found</h3></div>`;
            usersLoaded = true;
            return;
        }

        wrap.innerHTML = `
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                    </tr>
                </thead>
                <tbody>
                    ${users.map(u => `
                    <tr>
                        <td class="td-name">${[u.firstName, u.lastName].filter(Boolean).join(' ') || '—'}</td>
                        <td class="td-email">${u.email || '—'}</td>
                        <td><span class="role-badge role-${(u.role||'viewer').toLowerCase()}">${u.role || 'VIEWER'}</span></td>
                    </tr>`).join('')}
                </tbody>
            </table>`;
        usersLoaded = true;
    } catch (err) {
        wrap.innerHTML = `<div class="empty-state"><h3>Failed to load users</h3><p>${err.message}</p></div>`;
    }
}

/* ─── Create civilization ─── */
document.getElementById('create-civ-form').addEventListener('submit', async e => {
    e.preventDefault();
    const btn = document.getElementById('create-civ-btn');
    btn.disabled = true;
    btn.textContent = 'Creating…';

    try {
        const civId = await CivAPI.create({
            title:      document.getElementById('civ-title').value.trim(),
            description:document.getElementById('civ-desc').value.trim(),
            startYear:  parseInt(document.getElementById('civ-start').value),
            endYear:    parseInt(document.getElementById('civ-end').value),
            commitMsg:  document.getElementById('civ-commit').value.trim()
        });
        showToast('Civilization created', 'success');
        closeModal('modal-create-civ');
        setTimeout(() => window.location.href = `civilization.html?civId=${civId}`, 500);
    } catch (err) {
        showToast(err.message || 'Failed to create', 'error');
        btn.disabled = false;
        btn.textContent = 'Create';
    }
});

/* ─── Modal helpers ─── */
function openModal(id)  { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

document.addEventListener('click', e => {
    if (e.target.classList.contains('modal-overlay')) e.target.classList.remove('open');
});

/* ─── Toast ─── */
function showToast(message, type = 'info') {
    const c = document.getElementById('toast-container');
    const t = document.createElement('div');
    t.className = `toast toast-${type}`;
    t.textContent = message;
    c.appendChild(t);
    setTimeout(() => { t.style.opacity='0'; t.style.transition='0.2s'; setTimeout(()=>t.remove(),200); }, 3500);
}

/* ─── Boot ─── */
initUI();
loadCivilizations();