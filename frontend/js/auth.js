// ============================================================
//  CRAFTED CHARMS — Auth Page JS (API-connected)
// ============================================================

/* ── Tab switching ── */
const tabs = {
  signin:   { tab: document.getElementById('tab-signin'),   form: document.getElementById('form-signin')   },
  register: { tab: document.getElementById('tab-register'), form: document.getElementById('form-register') },
};

function switchTab(active) {
  Object.entries(tabs).forEach(([key, { tab, form }]) => {
    const isActive = key === active;
    tab.classList.toggle('active', isActive);
    form.classList.toggle('active', isActive);
  });
}

tabs.signin.tab.addEventListener('click',   () => switchTab('signin'));
tabs.register.tab.addEventListener('click', () => switchTab('register'));
document.getElementById('go-register').addEventListener('click', e => { e.preventDefault(); switchTab('register'); });
document.getElementById('go-signin').addEventListener('click',   e => { e.preventDefault(); switchTab('signin');   });

/* ── If already logged in, redirect ── */
if (typeof Auth !== 'undefined' && Auth.isLoggedIn()) {
  window.location.href = '../../index.html';
}

/* ── Validation helpers ── */
function validateEmail(v)    { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim()); }
function validatePassword(v) { return v.length >= 8; }

function setError(inputId, errorId, hasError) {
  const input = document.getElementById(inputId);
  const err   = document.getElementById(errorId);
  input.classList.toggle('error', hasError);
  err.classList.toggle('visible', hasError);
  return !hasError;
}

/* ── Sign In ── */
document.getElementById('signin-form').addEventListener('submit', async e => {
  e.preventDefault();
  const username = document.getElementById('signin-email').value.trim();
  const password = document.getElementById('signin-password').value;
  let valid = true;
  valid = setError('signin-email',    'signin-email-error',    username.length < 2)    && valid;
  valid = setError('signin-password', 'signin-password-error', !validatePassword(password)) && valid;
  if (!valid) return;

  const btn = document.getElementById('signin-submit-btn');
  btn.textContent = 'Signing in…'; btn.disabled = true;
  try {
    const res = await AuthAPI.login({ username, password });
    Auth.setToken(res.token);
    Auth.setUser(res.user);
    showToast('Welcome back, ' + res.user.fullName.split(' ')[0] + '!', 'success');
    setTimeout(() => {
      if (res.user.role === 'ADMIN') window.location.href = '../pages/admin.html';
      else window.location.href = '../../index.html';
    }, 900);
  } catch (err) {
    showToast(err.message, 'error');
    btn.textContent = 'Sign In'; btn.disabled = false;
  }
});

/* ── Register ── */
document.getElementById('register-form').addEventListener('submit', async e => {
  e.preventDefault();
  const fullName = document.getElementById('reg-name').value.trim();
  const email    = document.getElementById('reg-email').value.trim();
  const password = document.getElementById('reg-password').value;
  const username = email.split('@')[0].toLowerCase().replace(/[^a-z0-9]/g, '');
  let valid = true;
  valid = setError('reg-name',     'reg-name-error',     fullName.length < 2)         && valid;
  valid = setError('reg-email',    'reg-email-error',    !validateEmail(email))        && valid;
  valid = setError('reg-password', 'reg-password-error', !validatePassword(password)) && valid;
  if (!valid) return;

  const btn = document.getElementById('register-submit-btn');
  btn.textContent = 'Creating account…'; btn.disabled = true;
  try {
    const res = await AuthAPI.register({ fullName, username, email, password });
    Auth.setToken(res.token);
    Auth.setUser(res.user);
    showToast('Welcome to Crafted Charms! 🪷', 'success');
    setTimeout(() => window.location.href = '../../index.html', 1000);
  } catch (err) {
    showToast(err.message, 'error');
    btn.textContent = 'Create Account'; btn.disabled = false;
  }
});

/* ── Toast ── */
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span class="toast-dot"></span>${message}`;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3200);
}
