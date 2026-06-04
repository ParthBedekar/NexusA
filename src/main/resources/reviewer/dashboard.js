import {
    isLoggedIn, logout,
    getAllLatestVersions,
    getMyMarks, markEntry,
    getCentralCivilizations, getCentralDetail,
    createCentralCivilization, addVolume, addEntry, flagDivergence,
    getCivMetadata
} from './api.js';

// ── Auth Guard ────────────────────────────────────────────────────────────
if (!isLoggedIn()) { window.location.href = 'login.html'; }

// ── State Management ──────────────────────────────────────────────────────
let currentView                 = 'civilizations';
let currentCivId                = null;
let currentCivTitle             = '';    // Tracks open context for target compiling
let currentVolId                = null;
let approvedMarks               = [];
let selectedEntriesForDivergence = [];   // Track entries selected for conflict marking
let selectedCivMetadata         = null;

// ── JWT Payload Extraction ────────────────────────────────────────────────
function decodeJwt(token) {
    try {
        return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    } catch { return {}; }
}
const token    = localStorage.getItem('reviewer_token');
const payload  = decodeJwt(token || '');
const revEmail = payload.sub || '';

document.getElementById('nav-reviewer-name').textContent = revEmail;
const initial = revEmail.charAt(0).toUpperCase();
document.getElementById('sidebar-avatar').textContent = initial;
document.getElementById('sidebar-name').textContent   = revEmail;

// ── Toast Notifications ───────────────────────────────────────────────────
function toast(msg, type = 'info') {
    const el = document.createElement('div');
    el.className = `toast toast-${type}`;
    el.textContent = msg;
    document.getElementById('toasts').appendChild(el);
    setTimeout(() => el.remove(), 3400);
}

// ── View Routing ──────────────────────────────────────────────────────────
function showView(name) {
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
    document.getElementById('view-' + name).classList.add('active');
    document.querySelectorAll('.sidebar-link').forEach(l => {
        l.classList.toggle('active', l.dataset.view === name);
    });
    document.getElementById('panel-central').classList.remove('open');
    currentView = name;
}

document.querySelectorAll('[data-view]').forEach(btn => {
    btn.addEventListener('click', () => {
        showView(btn.dataset.view);
        if (btn.dataset.view === 'civilizations') loadCivilizations();
        if (btn.dataset.view === 'marks')        loadMarks();
        if (btn.dataset.view === 'central')       loadCentral();
    });
});

// ── Sign Out ──────────────────────────────────────────────────────────────
document.getElementById('btn-signout').addEventListener('click', logout);

// ── Modal Handlers ────────────────────────────────────────────────────────
function openModal(id)  { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

document.querySelectorAll('[data-close]').forEach(btn => {
    btn.addEventListener('click', () => closeModal(btn.dataset.close));
});
document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', e => {
        if (e.target === overlay) overlay.classList.remove('open');
    });
});

// ── Formatting Utilities ──────────────────────────────────────────────────
function formatYear(y) {
    if (y == null) return '?';
    return y < 0 ? `${Math.abs(y)} BCE` : `${y} CE`;
}
function fmtDate(ts) {
    if (!ts) return '—';
    return new Date(ts).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}
function statusBadge(s) {
    const standardStatus = s || 'PENDING';
    return `<span class="status-badge status-${standardStatus}">${standardStatus.replace(/_/g, ' ')}</span>`;
}
function skeleton(n = 3) {
    return `<div class="skeleton-list">${'<div class="skeleton-row"></div>'.repeat(n)}</div>`;
}
function emptyState(icon, title, sub) {
    return `<div class="empty-state"><div class="empty-icon">${icon}</div><h3>${title}</h3><p>${sub}</p></div>`;
}
function escapeAttr(s) { return (s || '').replace(/"/g, '&quot;'); }
function numOrNull(id) {
    const v = document.getElementById(id)?.value.trim();
    return v === '' || v == null ? null : parseInt(v);
}

// ── Ingress Funnel: Incoming University Branches ─────────────────────────
async function loadCivilizations() {
    const el = document.getElementById('civs-list');
    el.innerHTML = skeleton();
    try {
        const versions = await getAllLatestVersions();
        document.getElementById('stat-civs').textContent  = versions.length;
        document.getElementById('pill-civs').textContent  = versions.length;
        if (!versions.length) {
            el.innerHTML = emptyState('◉', 'No incoming branches', 'No universities have submitted timeline proposals yet.');
            return;
        }
        el.innerHTML = versions.map(v => civRow(v)).join('');
        el.querySelectorAll('.version-row').forEach(row => {
            row.addEventListener('click', () =>
                openVersionDetail(row.dataset.id, row.dataset.civTitle));
        });
    } catch (e) {
        el.innerHTML = emptyState('⚠', 'Failed to load data stream', e.message);
    }
}

function civRow(v) {
    return `
    <div class="version-row" data-id="${v.versionId}" data-civ-title="${escapeAttr(v.civTitle || '')}">
      <div class="version-main">
        <div class="version-civ-name">${v.civTitle || 'Untitled Dataset'}</div>
        <div class="version-meta">
          <span class="prov-uni">Institution: ${v.universityName || 'Unknown University'}</span>
          <span>Committed By: ${v.committedByName || '—'}</span>
          <span>Received: ${fmtDate(v.commitTimestamp)}</span>
          ${v.commitMessage ? `<span class="commit-msg">"${v.commitMessage}"</span>` : ''}
        </div>
      </div>
      <span class="version-hash">${(v.hash || '').substring(0, 10)}</span>
      ${statusBadge(v.reviewStatus)}
      <span class="version-arrow">→</span>
    </div>`;
}

function openVersionDetail(versionId, civTitle) {
    window.location.href = `reviewer-civilization.html?versionId=${encodeURIComponent(versionId)}&civTitle=${encodeURIComponent(civTitle || '')}`;
}

document.getElementById('btn-refresh-civs').addEventListener('click', loadCivilizations);

// ── Review Audit Footprint (Granular Entry Ledger) ───────────────────────
async function loadMarks() {
    const el = document.getElementById('marks-list');
    el.innerHTML = skeleton();
    try {
        const marks = await getMyMarks();
        document.getElementById('stat-marks').textContent = marks.length;
        if (!marks.length) {
            el.innerHTML = emptyState('✓', 'Audit ledger empty', 'Granular decisions will appear here as you verify individual nodes.');
            return;
        }
        el.innerHTML = marks.map(m => markRow(m)).join('');
    } catch (e) {
        el.innerHTML = emptyState('⚠', 'Failed to load ledger', e.message);
    }
}

function markRow(m) {
    const statusColors = {
        APPROVED:           'background:#D1FAE5;color:#065F46;border:1px solid #A7F3D0',
        REJECTED:           'background:#FEE2E2;color:#991B1B;border:1px solid #FCA5A5',
        REVISION_REQUESTED: 'background:#FEF3C7;color:#92400E;border:1px solid #FDE68A',
    };
    const style = statusColors[m.markStatus] || '';
    return `
    <div class="version-row readonly">
      <div class="version-main">
        <div class="version-civ-name">${m.entryTitle || 'Untitled Document/Node'}</div>
        <div class="version-meta">
          <span>Source Block ID: ${(m.nodeId || '').substring(0, 8)}…</span>
          <span>Audited On: ${fmtDate(m.markedAt)}</span>
          ${m.reviewerNote ? `<span class="reviewer-note-bubble">Note: ${m.reviewerNote}</span>` : ''}
        </div>
      </div>
      <span class="status-badge" style="${style}">${m.markStatus.replace(/_/g, ' ')}</span>
    </div>`;
}

document.getElementById('btn-refresh-marks').addEventListener('click', loadMarks);

// ── Central Master Compiler Index ────────────────────────────────────────
async function loadCentral() {
    const el = document.getElementById('central-grid');
    el.innerHTML = skeleton(4);
    try {
        const civs = await getCentralCivilizations();
        if (!civs.length) {
            el.innerHTML = emptyState('⊞', 'Master Archive Empty', 'No standardized cross-institutional records compiled yet.');
            return;
        }
        el.innerHTML = civs.map(c => centralCard(c)).join('');
        el.querySelectorAll('.central-card').forEach(card => {
            card.addEventListener('click', () => openCentralDetail(card.dataset.id));
        });
    } catch (e) {
        el.innerHTML = emptyState('⚠', 'Failed to read index', e.message);
    }
}

function centralCard(c) {
    return `
    <div class="central-card" data-id="${c.centralCivId}">
      <div class="central-card-top">
        <span class="central-card-title">${c.title}</span>
      </div>
      <p class="central-card-desc">${c.description || '<em>No description provided.</em>'}</p>
      <div class="central-card-footer">
        <span class="years-badge">${formatYear(c.startYear)} – ${formatYear(c.endYear)}</span>
        <div class="central-card-counts">
          <span>${c.volumeCount} Vol${c.volumeCount !== 1 ? 's' : ''}</span>
          <span>${c.entryCount} Node${c.entryCount !== 1 ? 's' : ''}</span>
        </div>
      </div>
    </div>`;
}

// ── Central Panel Orchestration Workspace ────────────────────────────────
async function openCentralDetail(centralCivId) {
    currentCivId = centralCivId;
    selectedEntriesForDivergence = [];
    const panel   = document.getElementById('panel-central');
    const content = document.getElementById('central-detail-content');
    panel.classList.add('open');
    content.innerHTML = skeleton(3);
    try {
        const civ = await getCentralDetail(centralCivId);
        currentCivTitle = civ.title || ''; // Cache active workspace context for downstream filters
        content.innerHTML = renderCentralDetail(civ);
        wireCentralDetailButtons(civ);
    } catch (e) {
        content.innerHTML = `<p style="padding:20px;color:#991B1B;font-weight:500;">${e.message}</p>`;
    }
}

document.getElementById('btn-back-central').addEventListener('click', () => {
    document.getElementById('panel-central').classList.remove('open');
});

function renderCentralDetail(civ) {
    const volumes = (civ.volumes || []).map(vol => `
    <div class="volume-block">
      <div class="volume-header">
        <span class="volume-title">${vol.title}</span>
        <div style="display:flex;align-items:center;gap:10px">
          <span class="volume-years">${formatYear(vol.startYear)} – ${formatYear(vol.endYear)}</span>
          <button class="btn-primary" style="padding:5px 12px;font-size:0.78rem" data-add-entry-vol="${vol.volumeId}">+ Link Entry</button>
        </div>
      </div>
      ${vol.entries && vol.entries.length
        ? vol.entries.map(e => entryRow(e)).join('')
        : '<div style="padding:14px 16px;color:var(--faint);font-size:0.82rem;font-style:italic">No cross-institutional entries linked to this phase yet.</div>'}
    </div>`).join('');

    return `
    <div class="detail-header">
      <div>
        <h2 class="detail-title">${civ.title}</h2>
        <div class="detail-meta">
          <span>System Scope: ${formatYear(civ.startYear)} – ${formatYear(civ.endYear)}</span>
          <span>Curator: ${civ.createdByName || '—'}</span>
          <span>Last Core Compilation: ${fmtDate(civ.lastUpdatedAt)}</span>
        </div>
      </div>
      <div style="display:flex;gap:8px;flex-wrap:wrap">
        <button class="btn-primary" id="btn-add-volume">+ Add Phase/Volume</button>
        <button class="btn-primary" style="background:#4B5563;opacity:0.6;cursor:not-allowed" id="btn-flag-div" disabled>⚑ Flag Divergence (Select 2)</button>
      </div>
    </div>
    ${civ.description ? `<p class="central-detail-description-block">${civ.description}</p>` : ''}
    <div class="detail-section">
      <div class="section-context-bar">
        <span class="section-label">Compiled Chronology Ledger</span>
        <small style="color:var(--faint);font-size:0.75rem">Select two entry rows below to flag conflicting data weights.</small>
      </div>
      ${volumes || emptyState('📚', 'No volumes mapped', 'Define chronological framework parameters to begin schema construction.')}
    </div>`;
}

function entryRow(e) {
    const divBadge = e.isDivergent ? `<span class="divergence-badge">⚑ Divergent Context</span>` : '';

    // Fallback chain ensuring a descriptive name is ALWAYS rendered instead of a generic string
    const humanReadableTitle = e.entryTitle || e.title || e.nodeTitle || 'Unnamed Timeline Node';

    return `
    <div class="entry-row dynamic-selectable-entry" data-entry-id="${e.entryId}" data-entry-title="${escapeAttr(humanReadableTitle)}">
      <div style="display:flex;align-items:center;gap:12px;min-width:0;flex:1">
         <input type="checkbox" class="divergence-selector-checkbox" style="pointer-events:none;">
         <span class="entry-title" style="font-weight:500;">${humanReadableTitle}</span>
      </div>
      <span class="entry-meta">${formatYear(e.startYear)} – ${formatYear(e.endYear)}</span>
      ${divBadge}
      <span class="provenance-tag" title="Source Dataset Provenance">${e.sourceUniversityName || 'Institutional Branch'}</span>
    </div>`;
}

function wireCentralDetailButtons(civ) {
    document.getElementById('btn-add-volume').addEventListener('click', () => openModal('modal-add-volume'));

    const divergenceSubmitBtn = document.getElementById('btn-flag-div');
    divergenceSubmitBtn.addEventListener('click', () => {
        if (selectedEntriesForDivergence.length === 2) {
            openModal('modal-flag-divergence');
            document.getElementById('div-primary-id').value = selectedEntriesForDivergence[0].id;
            document.getElementById('div-primary-name').textContent = selectedEntriesForDivergence[0].title;
            document.getElementById('div-conflict-id').value = selectedEntriesForDivergence[1].id;
            document.getElementById('div-conflict-name').textContent = selectedEntriesForDivergence[1].title;
        }
    });

    // Granular Row Selector Logic for Contextual Multi-Selection
    document.querySelectorAll('.dynamic-selectable-entry').forEach(row => {
        row.addEventListener('click', () => {
            const entryId = row.dataset.entryId;
            const entryTitle = row.dataset.entryTitle;
            const checkbox = row.querySelector('.divergence-selector-checkbox');
            const existingIndex = selectedEntriesForDivergence.findIndex(item => item.id === entryId);

            if (existingIndex > -1) {
                selectedEntriesForDivergence.splice(existingIndex, 1);
                row.classList.remove('selected-for-divergence');
                if (checkbox) checkbox.checked = false;
            } else {
                if (selectedEntriesForDivergence.length >= 2) {
                    toast('Divergence maps link two conflicting nodes. Deselect an entry first.', 'info');
                    return;
                }
                selectedEntriesForDivergence.push({ id: entryId, title: entryTitle });
                row.classList.add('selected-for-divergence');
                if (checkbox) checkbox.checked = true;
            }

            // Sync structural status flags on control panel
            if (selectedEntriesForDivergence.length === 2) {
                divergenceSubmitBtn.removeAttribute('disabled');
                divergenceSubmitBtn.style.background = 'var(--rev-ink)';
                divergenceSubmitBtn.style.opacity = '1';
                divergenceSubmitBtn.style.cursor = 'pointer';
                divergenceSubmitBtn.textContent = '⚑ Flag Divergence';
            } else {
                divergenceSubmitBtn.setAttribute('disabled', 'true');
                divergenceSubmitBtn.style.background = '#4B5563';
                divergenceSubmitBtn.style.opacity = '0.6';
                divergenceSubmitBtn.style.cursor = 'not-allowed';
                divergenceSubmitBtn.textContent = `⚑ Flag Divergence (${selectedEntriesForDivergence.length}/2 Select)`;
            }
        });
    });

    document.querySelectorAll('[data-add-entry-vol]').forEach(btn => {
        btn.addEventListener('click', e => {
            e.stopPropagation();
            currentVolId = btn.dataset.addEntryVol;
            openAddEntryModal();
        });
    });
}

// ── Chronology Compiler: Map Approved Elements (Scoped Filter) ───────────
// ── Chronology Compiler: Map Approved Elements (Scoped Filter) ───────────
async function openAddEntryModal() {
    openModal('modal-add-entry');

    // Clear out position parameters (the strict ID fields are removed or hidden)
    document.getElementById('entry-position').value = '';

    const listEl = document.getElementById('approved-marks-list');
    listEl.innerHTML = '<div style="padding:20px;text-align:center;color:var(--faint);font-size:0.82rem">Reading approved institutional cache…</div>';

    try {
        const marks = await getMyMarks();
        console.log("Raw Marks Data Stream from Backend:", marks); // Diagnostic Log

        const targetTitle = (currentCivTitle || '').toLowerCase().trim();

        // Filter out status configurations seamlessly
        approvedMarks = marks.filter(m => {
            if (!m.markStatus) return false;

            const isApproved = m.markStatus.toUpperCase() === 'APPROVED';

            const matchesContext = !targetTitle ||
                (m.entryTitle || '').toLowerCase().includes(targetTitle) ||
                (m.civTitle || '').toLowerCase().includes(targetTitle) ||
                (m.civilizationName || '').toLowerCase().includes(targetTitle);

            return isApproved && matchesContext;
        });

        // FALLBACK: If filtering hid valid nodes, show all approved items to prevent deadlocks
        if (!approvedMarks.length && marks.some(m => m.markStatus?.toUpperCase() === 'APPROVED')) {
            console.warn("Context filter hidden items. Falling back to all approved nodes.");
            approvedMarks = marks.filter(m => m.markStatus.toUpperCase() === 'APPROVED');
        }

        if (!approvedMarks.length) {
            listEl.innerHTML = `
                <div style="padding:20px;text-align:center;color:var(--faint);font-size:0.82rem">
                    No approved timeline modifications match <strong>"${currentCivTitle || 'Active Civ'}"</strong>.<br>
                    <small style="display:block;margin-top:6px;color:var(--faint)">Audit incoming university datasets for this civilization first.</small>
                </div>`;
            return;
        }

        // Render clean structural row cards - versionId and nodeId strings completely stripped from view
        listEl.innerHTML = approvedMarks.map((m, i) => `
            <div class="browser-row" data-idx="${i}" style="padding: 10px; border-bottom: 1px solid var(--rule); cursor: pointer;">
                <div style="flex:1;min-width:0">
                    <div style="font-size:0.875rem;font-weight:500;color:var(--ink)">${m.entryTitle || m.title || 'Untitled Record'}</div>
                    <div style="font-size:0.75rem;color:var(--faint);margin-top:2px">
                        Validated and Cleared via Institutional Footprint Ledger
                    </div>
                </div>
                <span style="font-family:var(--mono);font-size:0.65rem;background:#D1FAE5;color:#065F46;padding:2px 8px;border-radius:3px;flex-shrink:0">VALIDATED MATCH</span>
            </div>`).join('');

        // Selection interaction handler
        listEl.querySelectorAll('.browser-row').forEach(row => {
            row.addEventListener('click', () => {
                listEl.querySelectorAll('.browser-row').forEach(r => r.classList.remove('selected'));
                row.classList.add('selected');

                // Set inline selection highlight styling
                row.style.background = 'rgba(6, 95, 70, 0.05)';

                // Store selected tracking object reference seamlessly into parent context scope
                const chosenMarkIndex = parseInt(row.dataset.idx, 10);
                row.dataset.selectedMarkIndex = chosenMarkIndex;
            });
        });
    } catch (e) {
        console.error("Error executing layout generation loop:", e);
        listEl.innerHTML = `<div style="padding:16px;color:#991B1B;font-size:0.82rem">${e.message}</div>`;
    }
}

document.getElementById('btn-submit-entry').addEventListener('click', async () => {
    const selectedRow = document.querySelector('.browser-row.selected');

    if (!selectedRow) {
        toast('Select an approved entry from the list to link', 'error');
        return;
    }

    const m = approvedMarks[parseInt(selectedRow.dataset.idx, 10)];

    if (!m) {
        toast('Could not resolve selected entry. Please try again.', 'error');
        return;
    }

    const positionInput = document.getElementById('entry-position').value.trim();

    const btn = document.getElementById('btn-submit-entry');
    btn.disabled = true;
    btn.textContent = 'Linking Block…';

    try {
        const dto = {
            sourceVersionId: m.versionId,
            sourceNodeId:    m.nodeId,
            position:        positionInput ? parseInt(positionInput, 10) : 1,
        };

        await addEntry(currentCivId, currentVolId, dto);

        toast('Entry linked to archive successfully', 'success');
        closeModal('modal-add-entry');
        openCentralDetail(currentCivId);
    } catch (e) {
        toast(e.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Add to Archive';
    }
});

// ── Central Civilization Generation ──────────────────────────────────────
let searchTimeout = null;
const searchInput = document.getElementById('new-civ-search');
const pickerEl = document.getElementById('civ-metadata-picker');

// Reset states on new civ button click
document.getElementById('btn-new-civ').addEventListener('click', () => {
    searchInput.value = '';
    pickerEl.innerHTML = '';
    pickerEl.style.display = 'none';
    selectedCivMetadata = null;
    openModal('modal-new-civ');
});

searchInput.addEventListener('input', () => {
    clearTimeout(searchTimeout);
    const query = searchInput.value.trim();
    if (!query) {
        pickerEl.innerHTML = '';
        pickerEl.style.display = 'none';
        selectedCivMetadata = null;
        return;
    }
    searchTimeout = setTimeout(async () => {
        try {
            pickerEl.innerHTML = '<div style="padding: 10px; text-align: center; color: var(--faint);">Searching university records...</div>';
            pickerEl.style.display = 'block';
            
            const results = await getCivMetadata(query);
            if (!results || results.length === 0) {
                pickerEl.innerHTML = `<div style="padding: 16px; text-align: center; color: var(--faint); font-size: 0.85rem;">No matching university submissions found for "${query}"</div>`;
                selectedCivMetadata = null;
                return;
            }
            
            pickerEl.innerHTML = `
                <div style="font-size: 0.75rem; text-transform: uppercase; color: var(--faint); font-weight: 500; margin-bottom: 8px; font-family: var(--mono);">Select University Source Version</div>
                <div class="metadata-card-grid">
                    ${results.map((r, idx) => `
                        <div class="metadata-card" data-idx="${idx}">
                            <div class="metadata-card-header">
                                <span class="metadata-card-uni">${r.universityName || 'Unknown University'}</span>
                                <span class="metadata-card-years">${formatYear(r.startYear)} – ${formatYear(r.endYear)}</span>
                            </div>
                            <div style="font-weight: 600; font-size: 0.9rem; margin-bottom: 4px; color: var(--ink);">${r.title}</div>
                            <p class="metadata-card-desc">${r.description || '<em>No description provided.</em>'}</p>
                        </div>
                    `).join('')}
                </div>
            `;
            
            // Add click listener to cards
            pickerEl.querySelectorAll('.metadata-card').forEach(card => {
                card.addEventListener('click', () => {
                    pickerEl.querySelectorAll('.metadata-card').forEach(c => c.classList.remove('selected'));
                    card.classList.add('selected');
                    const idx = parseInt(card.dataset.idx, 10);
                    selectedCivMetadata = results[idx];
                });
            });
            
        } catch (e) {
            pickerEl.innerHTML = `<div style="padding: 16px; text-align: center; color: var(--rej-ink); font-size: 0.85rem;">Failed to fetch records: ${e.message}</div>`;
            selectedCivMetadata = null;
        }
    }, 300);
});

document.getElementById('btn-submit-new-civ').addEventListener('click', async () => {
    if (!selectedCivMetadata) {
        toast('Please select a university source card first', 'error');
        return;
    }

    const dto = {
        title: selectedCivMetadata.title,
        description: selectedCivMetadata.description,
        startYear: selectedCivMetadata.startYear,
        endYear: selectedCivMetadata.endYear
    };

    const btn = document.getElementById('btn-submit-new-civ');
    btn.disabled = true;
    btn.textContent = 'Generating Matrix…';

    try {
        await createCentralCivilization(dto);
        toast('Standardized Central Civilization Generated', 'success');
        closeModal('modal-new-civ');
        showView('central');
        loadCentral();
    } catch (e) {
        toast(e.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Create';
    }
});

// ── Structural Chronological Range Phase Definitions ──────────────────────
document.getElementById('btn-submit-volume').addEventListener('click', async () => {
    const dto = {
        title:     document.getElementById('vol-title').value.trim(),
        startYear: numOrNull('vol-start'),
        endYear:   numOrNull('vol-end'),
        position:  parseInt(document.getElementById('vol-position').value) || 1,
    };
    if (!dto.title) { toast('Phase boundary naming identification missing', 'error'); return; }
    const btn = document.getElementById('btn-submit-volume');
    btn.disabled = true; btn.textContent = 'Structuring…';
    try {
        await addVolume(currentCivId, dto);
        toast('Chronological Framework Partition Mapped', 'success');
        closeModal('modal-add-volume');
        openCentralDetail(currentCivId);
        ['vol-title','vol-start','vol-end','vol-position'].forEach(id => {
            document.getElementById(id).value = '';
        });
    } catch (e) {
        toast(e.message, 'error');
    } finally {
        btn.disabled = false; btn.textContent = 'Add Volume';
    }
});

// ── Multi-Institutional Structural Divergence Asserter ─────────────────────
document.getElementById('btn-submit-divergence').addEventListener('click', async () => {
    const dto = {
        primaryEntryId:     document.getElementById('div-primary-id').value.trim(),
        conflictingEntryId: document.getElementById('div-conflict-id').value.trim(),
        divergenceNote:     document.getElementById('div-note').value.trim(),
    };
    if (!dto.primaryEntryId || !dto.conflictingEntryId || !dto.divergenceNote) {
        toast('Disagreement assertion argument parameter required', 'error'); return;
    }
    const btn = document.getElementById('btn-submit-divergence');
    btn.disabled = true; btn.textContent = 'Registering Assertion…';
    try {
        await flagDivergence(currentCivId, dto);
        toast('Divergence constraint mapping successfully injected', 'success');
        closeModal('modal-flag-divergence');
        openCentralDetail(currentCivId);
        ['div-primary-id','div-conflict-id','div-note'].forEach(id => {
            document.getElementById(id).value = '';
        });
    } catch (e) {
        toast(e.message, 'error');
    } finally {
        btn.disabled = false; btn.textContent = 'Flag Divergence';
    }
});

// ── System Boot Sequence ──────────────────────────────────────────────────
loadCivilizations();