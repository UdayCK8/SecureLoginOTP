/**
 * SecureLoginOTP — Frontend JavaScript
 */

const API = '/api/auth';

async function post(endpoint, body) {
    try {
        const cfg = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'same-origin'
        };
        if (body) cfg.body = JSON.stringify(body);
        const res = await fetch(API + endpoint, cfg);
        return await res.json().catch(() => ({ success: false, message: 'Unexpected server response' }));
    } catch (e) {
        return { success: false, message: e.message || 'Network error — is the server running?' };
    }
}

async function get(endpoint) {
    try {
        const res = await fetch(API + endpoint, { method: 'GET', credentials: 'same-origin' });
        return await res.json().catch(() => ({ success: false, message: 'Unexpected server response' }));
    } catch (e) {
        return { success: false, message: e.message || 'Network error — is the server running?' };
    }
}

function showAlert(id, message, type) {
    const el = document.getElementById(id);
    if (!el) return;
    el.className = 'alert show alert-' + type;
    el.textContent = message;
}

function hideAlert(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.className = 'alert';
    el.textContent = '';
}

function setLoading(btn, loading) {
    if (!btn) return;
    if (loading) {
        btn._orig = btn.innerHTML;
        btn.innerHTML = '<span class="spinner" style="border-color:rgba(255,255,255,0.3);border-top-color:#fff;"></span> Please wait…';
        btn.disabled = true;
    } else {
        btn.innerHTML = btn._orig || 'Submit';
        btn.disabled = false;
    }
}

function validateEmail(email) { return /^[\w.+-]+@[\w-]+(\.[a-zA-Z]{2,})+$/.test(email.trim()); }
function validateUsername(u) { return /^[a-zA-Z0-9_]{4,20}$/.test(u.trim()); }
function validatePassword(p) { return p && p.length >= 8 && /[a-zA-Z]/.test(p) && /\d/.test(p); }
function validateOtp(o) { return /^\d{6}$/.test(o.trim()); }

async function checkAuth() {
    try { const d = await get('/me'); return d.success ? d.data : null; } catch { return null; }
}

async function requireAuth() {
    const u = await checkAuth();
    if (!u) { window.location.href = 'login.html'; }
    return u;
}

async function logout() { await post('/logout'); window.location.href = 'index.html'; }

/* Page initialisers */
document.addEventListener('DOMContentLoaded', () => {
    const page = document.body.dataset.page;
    if (page === 'register') initRegister();
    if (page === 'login') initLogin();
    if (page === 'change-password') initChangePassword();
    if (page === 'dashboard') initDashboard();
});

/* ========== Register ========== */
function initRegister() {
    const form = document.getElementById('registerForm');
    if (!form) return;
    form.addEventListener('submit', async e => {
        e.preventDefault();
        hideAlert('registerAlert');
        const u = document.getElementById('regUsername').value;
        const p = document.getElementById('regPassword').value;
        const em = document.getElementById('regEmail').value;

        if (!validateUsername(u)) { showAlert('registerAlert', 'Username: 4–20 letters/digits/underscores', 'warning'); return; }
        if (!validatePassword(p)) { showAlert('registerAlert', 'Password: ≥8 chars with at least 1 letter and 1 digit', 'warning'); return; }
        if (!validateEmail(em)) { showAlert('registerAlert', 'Enter a valid email address', 'warning'); return; }

        const btn = form.querySelector('button');
        setLoading(btn, true);
        const res = await post('/register', { username: u, password: p, email: em });
        setLoading(btn, false);

        if (res.success) {
            showAlert('registerAlert', 'Registered! Redirecting to login…', 'success');
            setTimeout(() => window.location.href = 'login.html', 1000);
        } else {
            showAlert('registerAlert', res.message || 'Registration failed', 'danger');
        }
    });
}

/* ========== Login ========== */
let otpTimerId = null;

function showStep(id) {
    document.querySelectorAll('.login-step').forEach(el => el.classList.add('hide'));
    const el = document.getElementById('step-' + id);
    if (el) el.classList.remove('hide');
}

function startOtpTimer(seconds) {
    const el = document.getElementById('otpTimer');
    const end = Date.now() + seconds * 1000;
    if (otpTimerId) clearInterval(otpTimerId);
    function tick() {
        const secs = Math.max(0, Math.ceil((end - Date.now()) / 1000));
        const mm = String(Math.floor(secs / 60)).padStart(2, '0');
        const ss = String(secs % 60).padStart(2, '0');
        el.textContent = mm + ':' + ss;
        if (secs <= 0) {
            clearInterval(otpTimerId);
            el.classList.add('done');
            showAlert('loginAlert', 'OTP expired. Click Resend or restart login.', 'danger');
        }
    }
    el.classList.remove('done');
    tick();
    otpTimerId = setInterval(tick, 1000);
}

function stopOtpTimer() {
    if (otpTimerId) { clearInterval(otpTimerId); otpTimerId = null; }
}

async function doIssueOtp(resend = false) {
    hideAlert('loginAlert');
    const btn = resend ? document.getElementById('resendBtn') : null;
    if (btn) setLoading(btn, true);
    const res = await post(resend ? '/login/otp/resend' : '/login/otp/issue');
    if (btn) setLoading(btn, false);
    if (res.success) {
        showAlert('loginAlert', res.message, 'success');
        showStep('otp');
        startOtpTimer(5 * 60);
    } else {
        showAlert('loginAlert', res.message || 'Failed to send OTP', 'danger');
        if (resend) showStep('otp');
    }
}

function initLogin() {
    const pf = document.getElementById('passwordForm');
    const of = document.getElementById('otpForm');
    const rb = document.getElementById('resendBtn');

    if (pf) {
        pf.addEventListener('submit', async e => {
            e.preventDefault();
            hideAlert('loginAlert');
            const u = document.getElementById('loginUsername').value.trim();
            const p = document.getElementById('loginPassword').value;
            const btn = pf.querySelector('button');
            setLoading(btn, true);
            const res = await post('/login/password', { username: u, password: p });
            setLoading(btn, false);
            if (res.success) {
                showAlert('loginAlert', res.message, 'success');
                showStep('wait');
                setTimeout(() => doIssueOtp(false), 900);
            } else {
                showAlert('loginAlert', res.message || 'Login failed', 'danger');
            }
        });
    }

    if (rb) {
        rb.addEventListener('click', () => {
            stopOtpTimer();
            doIssueOtp(true);
        });
    }

    if (of) {
        of.addEventListener('submit', async e => {
            e.preventDefault();
            hideAlert('loginAlert');
            const otp = document.getElementById('otpInput').value.trim();
            if (!validateOtp(otp)) { showAlert('loginAlert', 'Enter a valid 6-digit OTP', 'warning'); return; }
            const btn = of.querySelector('button');
            setLoading(btn, true);
            const res = await post('/login/otp/verify', { otp });
            setLoading(btn, false);
            if (res.success) {
                stopOtpTimer();
                showAlert('loginAlert', 'Welcome! Redirecting…', 'success');
                setTimeout(() => window.location.href = 'dashboard.html', 600);
            } else {
                showAlert('loginAlert', res.message || 'Verification failed', 'danger');
                const fatal = ['Maximum OTP attempts', 'expired', 'already used', 'No active OTP'];
                if (fatal.some(f => res.message && res.message.includes(f))) stopOtpTimer();
            }
        });
    }
}

/* ========== Change Password ========== */
function initChangePassword() {
    const form = document.getElementById('changePasswordForm');
    if (!form) return;
    form.addEventListener('submit', async e => {
        e.preventDefault();
        hideAlert('cpAlert');
        const u = document.getElementById('cpUsername').value.trim();
        const oldP = document.getElementById('cpOldPassword').value;
        const newP = document.getElementById('cpNewPassword').value;
        const confirm = document.getElementById('cpConfirmPassword').value;

        if (!validatePassword(newP)) { showAlert('cpAlert', 'New password must be ≥8 chars with a letter and a digit', 'warning'); return; }
        if (newP !== confirm) { showAlert('cpAlert', 'Passwords do not match', 'warning'); return; }

        const btn = form.querySelector('button');
        setLoading(btn, true);
        const res = await post('/change-password', { username: u, oldPassword: oldP, newPassword: newP });
        setLoading(btn, false);
        if (res.success) {
            showAlert('cpAlert', 'Password changed! Redirecting to login…', 'success');
            setTimeout(() => window.location.href = 'login.html', 1200);
        } else {
            showAlert('cpAlert', res.message || 'Password change failed', 'danger');
        }
    });
}

/* ========== Dashboard ========== */
async function initDashboard() {
    const user = await requireAuth();
    if (!user) return;
    const el = document.getElementById('welcomeUser');
    if (el) el.textContent = user;
    document.querySelectorAll('[data-action="logout"]').forEach(b => {
        b.addEventListener('click', logout);
    });
}
