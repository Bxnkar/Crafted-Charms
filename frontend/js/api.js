// ============================================================
//  CRAFTED CHARMS — Frontend API Client
//  Update API_BASE_URL to your deployed backend URL in production.
// ============================================================

const API_BASE_URL = 'http://localhost:5001/api';

// ── JWT Token helpers ─────────────────────────────────────────
const Auth = {
  getToken:   ()        => localStorage.getItem('cc_token'),
  setToken:   (t)       => localStorage.setItem('cc_token', t),
  getUser:    ()        => JSON.parse(localStorage.getItem('cc_user') || 'null'),
  setUser:    (u)       => localStorage.setItem('cc_user', JSON.stringify(u)),
  clear:      ()        => { localStorage.removeItem('cc_token'); localStorage.removeItem('cc_user'); },
  isLoggedIn: ()        => !!localStorage.getItem('cc_token'),
  isAdmin:    ()        => Auth.getUser()?.role === 'ADMIN',
};

// ── Core fetch wrapper ────────────────────────────────────────
async function apiFetch(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  const token = Auth.getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(API_BASE_URL + path, { ...options, headers });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || `Request failed: ${res.status}`);
  return data;
}

// ── Products API ──────────────────────────────────────────────
const ProductsAPI = {
  getAll:        (params = {}) => apiFetch('/products?' + new URLSearchParams(params)),
  getById:       (id)          => apiFetch(`/products/${id}`),
  getAllAdmin:    ()            => apiFetch('/products/all'),
  create:        (body)        => apiFetch('/products',    { method: 'POST', body: JSON.stringify(body) }),
  update:        (id, body)    => apiFetch(`/products/${id}`, { method: 'PUT',  body: JSON.stringify(body) }),
  delete:        (id)          => apiFetch(`/products/${id}`, { method: 'DELETE' }),
};

// ── Auth API ──────────────────────────────────────────────────
const AuthAPI = {
  login:    (body) => apiFetch('/auth/login',    { method: 'POST', body: JSON.stringify(body) }),
  register: (body) => apiFetch('/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  me:       ()     => apiFetch('/auth/me'),
};

// ── Orders API ────────────────────────────────────────────────
const OrdersAPI = {
  place:       (body) => apiFetch('/orders',          { method: 'POST', body: JSON.stringify(body) }),
  getMyOrders: ()     => apiFetch('/orders/my'),
  getAll:      ()     => apiFetch('/orders'),
  updateStatus:(id, status) => apiFetch(`/orders/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
};

// ── Users API ─────────────────────────────────────────────────
const UsersAPI = {
  getAll:  ()       => apiFetch('/users'),
  create:  (body)   => apiFetch('/users', { method: 'POST', body: JSON.stringify(body) }),
  update:  (id, b)  => apiFetch(`/users/${id}`, { method: 'PUT',    body: JSON.stringify(b) }),
  delete:  (id)     => apiFetch(`/users/${id}`,  { method: 'DELETE' }),
};
