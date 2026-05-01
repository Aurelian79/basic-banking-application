// ─────────────────────────────────────────────
//  auth.js  — wired to AuthServlet /api/auth/*
// ─────────────────────────────────────────────

window.API_BASE = window.API_BASE || '/banking-system/api';

function switchTab(tab) {
  document.getElementById('loginSection').classList.toggle('active', tab === 'login');
  document.getElementById('registerSection').classList.toggle('active', tab === 'register');
  document.querySelectorAll('.tab').forEach((t, i) => {
    t.classList.toggle('active', (i === 0 && tab === 'login') || (i === 1 && tab === 'register'));
  });
  clearMessages();
}

function showMsg(id, text, type) {
  const el = document.getElementById(id);
  el.textContent = text;
  el.className = 'msg ' + type;
}

function clearMessages() {
  ['loginMsg', 'registerMsg'].forEach(id => {
    const el = document.getElementById(id);
    if (el) { el.textContent = ''; el.className = 'msg'; }
  });
}

// ── LOGIN ──────────────────────────────────────
async function handleLogin() {
  const email    = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;
  const btn      = document.getElementById('loginBtn');

  if (!email || !password) {
    showMsg('loginMsg', 'Please fill in all fields.', 'error');
    return;
  }

  btn.disabled = true;
  btn.textContent = 'Signing in…';

  try {
    const res = await fetch(`${window.API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',                 // sends JSESSIONID cookie
      body: JSON.stringify({ email, password })
    });

    const data = await res.json();

    if (!res.ok) {
      showMsg('loginMsg', data.error || 'Login failed.', 'error');
      return;
    }

    // Store user info in sessionStorage for UI use (not auth — session cookie handles auth)
    sessionStorage.setItem('user', JSON.stringify(data));

    // Route by role
    if (data.role === 'ADMIN') {
      window.location.href = 'admin.html';
    } else {
      window.location.href = 'dashboard.html';
    }

  } catch (err) {
    showMsg('loginMsg', 'Network error. Is Tomcat running?', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Sign In';
  }
}

// ── REGISTER ───────────────────────────────────
async function handleRegister() {
  const fullName = document.getElementById('regName').value.trim();
  const email    = document.getElementById('regEmail').value.trim();
  const phone    = document.getElementById('regPhone').value.trim();
  const password = document.getElementById('regPassword').value;
  const btn      = document.getElementById('registerBtn');

  if (!fullName || !email || !phone || !password) {
    showMsg('registerMsg', 'All fields are required.', 'error');
    return;
  }

  if (password.length < 6) {
    showMsg('registerMsg', 'Password must be at least 6 characters.', 'error');
    return;
  }

  btn.disabled = true;
  btn.textContent = 'Creating account…';

  try {
    const res = await fetch(`${window.API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify({ fullName, email, phone, password })
    });

    const data = await res.json();

    if (!res.ok) {
      showMsg('registerMsg', data.error || 'Registration failed.', 'error');
      return;
    }

    showMsg('registerMsg', 'Account created! Please sign in.', 'success');
    setTimeout(() => switchTab('login'), 1500);

  } catch (err) {
    showMsg('registerMsg', 'Network error. Is Tomcat running?', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Create Account';
  }
}

// ── LOGOUT (called from dashboard/admin) ───────
async function handleLogout() {
  try {
    await fetch(`${window.API_BASE}/auth/logout`, {
      method: 'POST',
      credentials: 'same-origin'
    });
  } finally {
    sessionStorage.clear();
    window.location.href = 'index.html';
  }
}

// ── Guard: redirect to login if no session user ─
function requireAuth() {
  const user = sessionStorage.getItem('user');
  if (!user) window.location.href = 'index.html';
  return JSON.parse(user);
}
