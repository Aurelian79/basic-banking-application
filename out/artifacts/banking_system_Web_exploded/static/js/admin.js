// ─────────────────────────────────────────────
//  admin.js — wired to LoanServlet, AccountServlet
//  Admin-only routes: approve/reject loans, freeze accounts
// ─────────────────────────────────────────────

const BASE = '/banking-system/api';

document.addEventListener('DOMContentLoaded', () => {
  const user = requireAuth();                        // from auth.js
  if (user.role !== 'ADMIN') {
    // Non-admins get bounced back
    window.location.href = 'dashboard.html';
    return;
  }
  document.getElementById('topbarUser').textContent = user.fullName || user.email;
  loadPendingLoans();
});

// ── NAVIGATION ─────────────────────────────────
function showSection(name) {
  document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  document.getElementById(name).classList.add('active');
  document.querySelector(`[data-section="${name}"]`).classList.add('active');

  const titles = { pendingLoans: 'Pending Loans', allLoans: 'All Loans', manageAccounts: 'Accounts' };
  document.getElementById('sectionTitle').textContent = titles[name];

  if (name === 'pendingLoans') loadPendingLoans();
}

// ── PENDING LOANS ──────────────────────────────
// NOTE: Your current LoanServlet only has /apply and GET /loans (by session user).
// To support admin viewing all pending loans, you need to add:
//   GET /api/loans/pending → LoanServlet → loanService.getPendingLoans()
// Until then this calls GET /loans which returns the session user's own loans.
// The approve/reject PATCH calls below are ready for when you add those routes.

async function loadPendingLoans() {
  const el = document.getElementById('pendingLoansList');
  el.innerHTML = '<div class="loading-text">Loading…</div>';
  try {
    // When you add GET /api/loans/pending to LoanServlet, change path here
    const loans = await apiFetch('/loans');
    const pending = loans.filter(l => l.status === 'PENDING');

    if (pending.length === 0) {
      el.innerHTML = '<div class="empty-state">No pending loans.</div>';
      return;
    }

    el.innerHTML = pending.map(l => `
      <div class="txn-item">
        <div class="txn-left">
          <span style="font-weight:600">Loan #${l.loanId} — ₹${fmt(l.loanAmount)}</span>
          <span class="txn-ref">User: ${l.userId} · Account: ${l.accountId} · ${l.tenureMonths} months</span>
        </div>
        <div class="txn-right" style="display:flex;align-items:center;gap:.75rem">
          <span class="badge PENDING">Pending</span>
          <div class="action-btns">
            <button class="action-btn approve" onclick="approveLoan(${l.loanId})">Approve</button>
            <button class="action-btn reject"  onclick="rejectLoan(${l.loanId})">Reject</button>
          </div>
        </div>
      </div>
    `).join('');
  } catch(e) {
    el.innerHTML = `<div class="loading-text" style="color:var(--error)">${e.message}</div>`;
  }
}

// ── APPROVE LOAN ───────────────────────────────
// Needs: PATCH /api/loans/{id}/approve in LoanServlet
async function approveLoan(loanId) {
  try {
    await apiFetch(`/loans/${loanId}/approve`, 'PATCH');
    showMsg('loanActionMsg', `Loan #${loanId} approved and funds disbursed.`, 'success');
    loadPendingLoans();
  } catch(e) {
    showMsg('loanActionMsg', e.message, 'error');
  }
}

// ── REJECT LOAN ────────────────────────────────
// Needs: PATCH /api/loans/{id}/reject in LoanServlet
async function rejectLoan(loanId) {
  try {
    await apiFetch(`/loans/${loanId}/reject`, 'PATCH');
    showMsg('loanActionMsg', `Loan #${loanId} rejected.`, 'success');
    loadPendingLoans();
  } catch(e) {
    showMsg('loanActionMsg', e.message, 'error');
  }
}

// ── ALL LOANS BY USER ──────────────────────────
async function loadAllLoans() {
  const userId = document.getElementById('allLoansUserId').value;
  if (!userId) return;

  const el = document.getElementById('allLoansList');
  el.innerHTML = '<div class="loading-text">Loading…</div>';

  try {
    // Needs: GET /api/loans?userId=X (admin override) in LoanServlet
    const loans = await apiFetch(`/loans?userId=${userId}`);
    if (!loans || loans.length === 0) {
      el.innerHTML = '<div class="empty-state">No loans found for this user.</div>';
      return;
    }
    el.innerHTML = loans.map(l => `
      <div class="txn-item">
        <div class="txn-left">
          <span style="font-weight:600">Loan #${l.loanId} — ₹${fmt(l.loanAmount)}</span>
          <span class="txn-ref">Account: ${l.accountId} · ${l.tenureMonths} months</span>
        </div>
        <div class="txn-right">
          <span class="badge ${l.status}">${l.status}</span>
        </div>
      </div>
    `).join('');
  } catch(e) {
    el.innerHTML = `<div class="loading-text" style="color:var(--error)">${e.message}</div>`;
  }
}

// ── FREEZE ACCOUNT ─────────────────────────────
// Needs: PATCH /api/accounts/{id}/freeze in AccountServlet
async function handleFreeze() {
  const accId = document.getElementById('freezeAccId').value;
  if (!accId) { showMsg('freezeMsg', 'Enter an account ID.', 'error'); return; }

  try {
    await apiFetch(`/accounts/${accId}/freeze`, 'PATCH');
    showMsg('freezeMsg', `Account ${accId} has been frozen.`, 'success');
  } catch(e) {
    showMsg('freezeMsg', e.message, 'error');
  }
}

// ── SHARED FETCH WRAPPER ───────────────────────
async function apiFetch(path, method = 'GET', body = null) {
  const opts = {
    method,
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json' }
  };
  if (body) opts.body = JSON.stringify(body);

  const res  = await fetch(`${BASE}${path}`, opts);
  const data = await res.json();

  if (!res.ok) {
    if (res.status === 401) { sessionStorage.clear(); window.location.href = 'index.html'; }
    throw new Error(data.error || 'Request failed');
  }

  return data;
}

// ── HELPERS ────────────────────────────────────
function fmt(n) {
  if (n === null || n === undefined) return '0.00';
  return parseFloat(n).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function showMsg(id, text, type) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = text;
  el.className   = 'msg ' + type;
  setTimeout(() => { el.className = 'msg'; el.textContent = ''; }, 5000);
}
