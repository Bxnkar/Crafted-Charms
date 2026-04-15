// ── Auth guard — redirect to login if not admin ────────────────
if (typeof Auth !== 'undefined') {
  if (!Auth.isLoggedIn()) {
    window.location.href = './auth.html';
  } else if (!Auth.isAdmin()) {
    alert('Admin access only.');
    window.location.href = '../../index.html';
  }
}

/* ── State (loaded from API) ── */
let products = [];
let orders   = [];
let users    = [];

let editingProductId = null;
let editingUserId    = null;

/* ── Load all data from MongoDB Atlas via API ── */
async function loadAllData() {
  try {
    [products, orders, users] = await Promise.all([
      ProductsAPI.getAllAdmin(),
      OrdersAPI.getAll(),
      UsersAPI.getAll()
    ]);
    // Update dashboard stats
    document.getElementById('stat-products').textContent = products.filter(p => p.isActive).length;
    document.getElementById('stat-orders').textContent   = orders.length;
    document.getElementById('stat-users').textContent    = users.filter(u => u.role === 'CUSTOMER').length;
    const revenue = orders.reduce((s, o) => s + (o.totalAmount || 0), 0);
    document.getElementById('stat-revenue').textContent  = '₹' + revenue.toLocaleString('en-IN');
  } catch (err) {
    showToast('Failed to load data: ' + err.message, 'error');
  }
}


/* ── Nav switching ── */
const sections = ['dashboard', 'products', 'orders', 'users'];
sections.forEach(key => {
  document.getElementById(`nav-${key}`).addEventListener('click', () => showSection(key));
});

function showSection(key) {
  sections.forEach(s => {
    document.getElementById(`section-${s}`).style.display  = s === key ? '' : 'none';
    document.getElementById(`nav-${s}`).classList.toggle('active', s === key);
  });
  if (key === 'products') renderProducts();
  if (key === 'orders')   renderOrders();
  if (key === 'users')    renderUsers();
}

/* ── Placeholder helper ── */
function imgCell(src) {
  if (src) return `<div class="td-img"><img src="${src}" alt="" style="width:100%;height:100%;object-fit:cover;border-radius:8px;" /></div>`;
  return `<div class="td-img">
    <div class="img-placeholder" style="height:100%; border-radius:8px; font-size:0.55rem; gap:4px;">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <rect x="3" y="3" width="18" height="18" rx="2"/>
        <circle cx="8.5" cy="8.5" r="1.5"/>
        <polyline points="21 15 16 10 5 21"/>
      </svg>
    </div>
  </div>`;
}

function badgeHtml(status) {
  const map = { active: 'badge-active', inactive: 'badge-inactive', delivered: 'badge-active', shipped: 'badge-pending', pending: 'badge-pending' };
  return `<span class="badge ${map[status] || 'badge-inactive'}">${status}</span>`;
}

/* ── Products ── */
function renderProducts() {
  const tbody = document.getElementById('products-tbody');
  tbody.innerHTML = products.map(p => `
    <tr>
      <td>${imgCell(p.imageUrl)}</td>
      <td><strong>${p.name}</strong></td>
      <td>₹${(p.price || 0).toLocaleString('en-IN')}</td>
      <td>${p.tag ? `<span class="badge badge-active">${p.tag}</span>` : '—'}</td>
      <td>${badgeHtml(p.isActive ? 'active' : 'inactive')}</td>
      <td>
        <div class="tbl-actions">
          <button class="btn-tbl-edit" data-pid="${p._id}">Edit</button>
          <button class="btn-tbl-del"  data-pid="${p._id}">Delete</button>
        </div>
      </td>
    </tr>`).join('');

  tbody.querySelectorAll('.btn-tbl-edit').forEach(btn =>
    btn.addEventListener('click', () => openProductModal(btn.dataset.pid)));
  tbody.querySelectorAll('.btn-tbl-del').forEach(btn =>
    btn.addEventListener('click', () => deleteProduct(btn.dataset.pid)));
}

async function deleteProduct(id) {
  if (!confirm('Deactivate this product?')) return;
  try {
    await ProductsAPI.delete(id);
    products = products.filter(p => p._id !== id);
    renderProducts();
    showToast('Product deactivated', 'error');
  } catch (err) { showToast(err.message, 'error'); }
}

/* ── Product Modal ── */
const productModal = document.getElementById('product-modal');

function openProductModal(id = null) {
  editingProductId = id;
  document.getElementById('product-modal-title').textContent = id ? 'Edit Product' : 'Add Product';
  const p = id ? products.find(x => x._id === id) : null;
  document.getElementById('p-name').value  = p ? p.name  : '';
  document.getElementById('p-desc').value  = p ? p.description  : '';
  document.getElementById('p-price').value = p ? p.price : '';
  document.getElementById('p-tag').value   = p ? p.tag   : '';
  const preview = document.getElementById('p-img-preview');
  if (p && p.imageUrl) { preview.src = p.imageUrl; preview.style.display = 'block'; }
  else            { preview.src = ''; preview.style.display = 'none'; }
  productModal.classList.add('active');
}
function closeProductModal() { productModal.classList.remove('active'); }

document.getElementById('add-product-btn').addEventListener('click', () => openProductModal());
document.getElementById('product-modal-close').addEventListener('click', closeProductModal);
document.getElementById('product-modal-cancel').addEventListener('click', closeProductModal);
productModal.addEventListener('click', e => { if (e.target === productModal) closeProductModal(); });

/* Image preview */
document.getElementById('p-img-file').addEventListener('change', function () {
  const file = this.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = e => {
    const preview = document.getElementById('p-img-preview');
    preview.src = e.target.result;
    preview.style.display = 'block';
  };
  reader.readAsDataURL(file);
});

document.getElementById('product-form').addEventListener('submit', async e => {
  e.preventDefault();
  const name     = document.getElementById('p-name').value.trim();
  const description = document.getElementById('p-desc').value.trim();
  const price    = Number(document.getElementById('p-price').value);
  const tag      = document.getElementById('p-tag').value.trim();
  const previewSrc = document.getElementById('p-img-preview').src || '';
  const imageUrl = previewSrc.startsWith('data:') ? previewSrc : (previewSrc || undefined);

  if (!name || !price) { showToast('Name and price are required', 'error'); return; }

  try {
    if (editingProductId) {
      const updated = await ProductsAPI.update(editingProductId, { name, description, price, tag, imageUrl });
      const idx = products.findIndex(p => p._id === editingProductId);
      if (idx > -1) products[idx] = updated;
      showToast('Product updated', 'success');
    } else {
      const created = await ProductsAPI.create({ name, description, price, tag, imageUrl, category:'RING', material:'Mixed', stockQty:10 });
      products.push(created);
      showToast('Product added', 'success');
    }
    closeProductModal();
    renderProducts();
  } catch (err) { showToast(err.message, 'error'); }
});

/* ── Orders ── */
function renderOrders() {
  const statusOptions = ['PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED'];
  const tbody = document.getElementById('orders-tbody');
  tbody.innerHTML = orders.map(o => `
    <tr>
      <td>#${(o._id || '').slice(-6).toUpperCase()}</td>
      <td>${o.customer?.fullName || '—'}</td>
      <td style="color:var(--mid); font-size:0.83rem;">${o.customer?.email || '—'}</td>
      <td>${(o.items || []).map(i => i.productName).join(', ') || '—'}</td>
      <td>₹${(o.totalAmount || 0).toLocaleString('en-IN')}</td>
      <td>
        <select class="badge" data-oid="${o._id}" style="border:none;background:transparent;cursor:pointer;font-size:0.7rem;font-weight:600;letter-spacing:0.04em;text-transform:uppercase;">
          ${statusOptions.map(s => `<option value="${s}" ${s === o.status ? 'selected' : ''}>${s}</option>`).join('')}
        </select>
      </td>
      <td><div class="tbl-actions"><button class="btn-tbl-del" data-oid="${o._id}">Remove</button></div></td>
    </tr>`).join('');

  tbody.querySelectorAll('select[data-oid]').forEach(sel =>
    sel.addEventListener('change', async () => {
      try {
        await OrdersAPI.updateStatus(sel.dataset.oid, sel.value);
        showToast('Status updated', 'success');
      } catch (err) { showToast(err.message, 'error'); }
    }));
  tbody.querySelectorAll('.btn-tbl-del[data-oid]').forEach(btn =>
    btn.addEventListener('click', () => {
      orders = orders.filter(x => x._id !== btn.dataset.oid);
      renderOrders();
      showToast('Order removed from view', 'error');
    }));
}

/* ── Users ── */
const userModal = document.getElementById('user-modal');

function renderUsers() {
  const tbody = document.getElementById('users-tbody');
  tbody.innerHTML = users.map(u => `
    <tr>
      <td><strong>${u.fullName}</strong></td>
      <td style="color:var(--mid); font-size:0.83rem;">${u.email}</td>
      <td><span class="badge ${u.role === 'ADMIN' ? 'badge-pending' : 'badge-active'}">${u.role}</span></td>
      <td style="color:var(--mid); font-size:0.83rem;">${new Date(u.createdAt).toLocaleDateString('en-IN')}</td>
      <td>${badgeHtml('active')}</td>
      <td>
        <div class="tbl-actions">
          <button class="btn-tbl-edit" data-uid="${u._id}">Edit</button>
          <button class="btn-tbl-del"  data-uid="${u._id}">Delete</button>
        </div>
      </td>
    </tr>`).join('');

  tbody.querySelectorAll('.btn-tbl-edit').forEach(btn =>
    btn.addEventListener('click', () => openUserModal(btn.dataset.uid)));
  tbody.querySelectorAll('.btn-tbl-del').forEach(btn =>
    btn.addEventListener('click', async () => {
      if (!confirm('Delete user?')) return;
      try {
        await UsersAPI.delete(btn.dataset.uid);
        users = users.filter(x => x._id !== btn.dataset.uid);
        renderUsers();
        showToast('User deleted', 'error');
      } catch (err) { showToast(err.message, 'error'); }
    }));
}

function openUserModal(id = null) {
  editingUserId = id;
  document.getElementById('user-modal-title').textContent = id ? 'Edit User' : 'Add User';
  const u = id ? users.find(x => x._id === id) : null;
  document.getElementById('u-name').value  = u ? u.fullName  : '';
  document.getElementById('u-email').value = u ? u.email : '';
  document.getElementById('u-role').value  = u ? u.role  : '';
  userModal.classList.add('active');
}
function closeUserModal() { userModal.classList.remove('active'); }

document.getElementById('add-user-btn').addEventListener('click', () => openUserModal());
document.getElementById('user-modal-close').addEventListener('click', closeUserModal);
document.getElementById('user-modal-cancel').addEventListener('click', closeUserModal);
userModal.addEventListener('click', e => { if (e.target === userModal) closeUserModal(); });

document.getElementById('user-form').addEventListener('submit', async e => {
  e.preventDefault();
  const fullName  = document.getElementById('u-name').value.trim();
  const email = document.getElementById('u-email').value.trim();
  const roleInput = document.getElementById('u-role').value.trim().toUpperCase();
  const role  = roleInput === 'ADMIN' ? 'ADMIN' : 'CUSTOMER';
  if (!fullName || !email) { showToast('Name and email are required', 'error'); return; }

  try {
    if (editingUserId) {
      const updated = await UsersAPI.update(editingUserId, { fullName, email, role });
      const idx = users.findIndex(x => x._id === editingUserId);
      if (idx > -1) users[idx] = updated;
      showToast('User updated', 'success');
    } else {
      const username = email.split('@')[0].toLowerCase().replace(/[^a-z0-9]/g, '');
      const tempPassword = 'ChangeMe123!';
      const created = await UsersAPI.create({ fullName, username, email, password: tempPassword, role });
      users.unshift(created);
      showToast('User added (default password: ChangeMe123!)', 'success');
    }
    closeUserModal();
    renderUsers();
  } catch (err) {
    showToast(err.message, 'error');
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

/* ── Init: load all data from MongoDB Atlas ── */
loadAllData();
