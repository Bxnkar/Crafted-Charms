// ============================================================
//  CRAFTED CHARMS — Main Frontend Script (API-connected)
// ============================================================

/* ── Reveal on scroll ── */
const revealObserver = new IntersectionObserver(
  (entries) => entries.forEach(e => e.isIntersecting && e.target.classList.add('revealed')),
  { threshold: 0.12 }
);
document.querySelectorAll('[data-reveal]').forEach(el => revealObserver.observe(el));

/* ── Navbar scroll shadow ── */
const navbar = document.getElementById('navbar');
window.addEventListener('scroll', () => {
  navbar.classList.toggle('scrolled', window.scrollY > 20);
}, { passive: true });

/* ── Hamburger menu ── */
const hamburger = document.getElementById('hamburger');
const navLinks  = document.getElementById('nav-links');
hamburger.addEventListener('click', () => {
  hamburger.classList.toggle('open');
  navLinks.classList.toggle('open');
});
navLinks.querySelectorAll('a').forEach(a =>
  a.addEventListener('click', () => {
    hamburger.classList.remove('open');
    navLinks.classList.remove('open');
  })
);

/* ── Auth Nav update ── */
function updateNavAuth() {
  const btn   = document.getElementById('nav-account-btn');
  const label = btn?.querySelector('.nav-account-label');
  if (Auth.isLoggedIn()) {
    if (label) label.textContent = Auth.getUser()?.fullName?.split(' ')[0] || 'Account';
    btn?.setAttribute('href', '#');
    btn?.addEventListener('click', e => {
      e.preventDefault();
      if (confirm('Log out?')) { Auth.clear(); location.reload(); }
    });
  }
}
updateNavAuth();

/* ── Cart State ── */
let cart = JSON.parse(localStorage.getItem('cc_cart') || '[]');

function saveCart()    { localStorage.setItem('cc_cart', JSON.stringify(cart)); }
function cartTotal()   { return cart.reduce((sum, item) => sum + item.price * item.qty, 0); }
function formatPrice(n){ return '₹' + n.toLocaleString('en-IN'); }

/* ── Render Cart ── */
function renderCart() {
  const body  = document.getElementById('cart-body');
  const count = document.getElementById('nav-bag-count');
  const total = document.getElementById('cart-subtotal-price');

  const totalQty = cart.reduce((s, i) => s + i.qty, 0);
  count.textContent = totalQty;
  count.classList.toggle('visible', totalQty > 0);
  total.textContent = formatPrice(cartTotal());

  if (cart.length === 0) {
    body.innerHTML = `
      <div class="cart-empty">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
          <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
          <path d="M16 10a4 4 0 01-8 0"/>
        </svg>
        <p>Your bag is empty.<br/>Start adding some charms!</p>
      </div>`;
    return;
  }

  body.innerHTML = cart.map((item, idx) => `
    <div class="cart-item">
      <div class="cart-item-img">
        ${item.imageUrl
          ? `<img src="${item.imageUrl}" alt="${item.name}" style="width:100%;height:100%;object-fit:cover;border-radius:8px;" />`
          : `<div class="img-placeholder" style="height:100%;border-radius:8px;font-size:0.6rem;">
               <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
             </div>`}
      </div>
      <div class="cart-item-details">
        <p class="cart-item-name">${item.name}</p>
        <p class="cart-item-price">${formatPrice(item.price)} × ${item.qty}</p>
        <span class="cart-item-remove" data-idx="${idx}">Remove</span>
      </div>
    </div>`).join('');

  body.querySelectorAll('.cart-item-remove').forEach(btn => {
    btn.addEventListener('click', () => {
      cart.splice(Number(btn.dataset.idx), 1);
      saveCart(); renderCart();
    });
  });
}

/* ── Open / Close Cart ── */
const cartDrawer  = document.getElementById('cart-drawer');
const cartOverlay = document.getElementById('cart-overlay');

function openCart()  { cartDrawer.classList.add('open'); cartOverlay.classList.add('active'); cartDrawer.setAttribute('aria-hidden','false'); }
function closeCart() { cartDrawer.classList.remove('open'); cartOverlay.classList.remove('active'); cartDrawer.setAttribute('aria-hidden','true'); }

document.getElementById('nav-bag-btn').addEventListener('click', openCart);
document.getElementById('cart-close-btn').addEventListener('click', closeCart);
cartOverlay.addEventListener('click', closeCart);

/* ── Checkout via API ── */
document.getElementById('cart-checkout-btn').addEventListener('click', async () => {
  if (cart.length === 0) { showToast('Your bag is empty!', 'error'); return; }
  if (!Auth.isLoggedIn()) {
    showToast('Please sign in to checkout', 'error');
    setTimeout(() => window.location.href = 'frontend/pages/auth.html', 900);
    return;
  }
  const user = Auth.getUser();
  const address = user.address ? `${user.address}, ${user.city}` : 'Mumbai, India';
  try {
    const items = cart.map(i => ({ productId: i.id, productName: i.name, quantity: i.qty }));
    const order = await OrdersAPI.place({ items, shippingAddress: address, paymentMethod: 'Cash on Delivery' });
    cart = []; saveCart(); renderCart(); closeCart();
    showToast(`Order #${order._id.slice(-6).toUpperCase()} placed! 🎉`, 'success');
  } catch (err) {
    showToast(err.message, 'error');
  }
});

/* ── Add to Bag ── */
document.querySelectorAll('.btn-add').forEach(btn => {
  btn.addEventListener('click', () => {
    const name  = btn.dataset.name;
    const price = Number(btn.dataset.price);
    const id    = btn.dataset.id;
    const imageUrl = btn.dataset.img || null;
    const existing = cart.find(i => i.name === name);
    if (existing) { existing.qty += 1; }
    else { cart.push({ id, name, price, qty: 1, imageUrl }); }
    saveCart(); renderCart(); openCart();
    showToast(`${name} added to bag`, 'success');
  });
});

/* ── Load Products from API ── */
async function loadProducts() {
  const grid = document.querySelector('.products-grid');
  if (!grid) return;
  try {
    const products = await ProductsAPI.getAll();
    if (products.length === 0) {
      grid.innerHTML = '<p style="color:var(--mid); padding:20px;">No products available yet.</p>';
      return;
    }
    grid.innerHTML = products.slice(0, 6).map(p => `
      <article class="product-card" data-reveal>
        <div class="product-img-wrap">
          ${p.imageUrl
            ? `<img src="${p.imageUrl}" alt="${p.name}" />`
            : `<div class="img-placeholder"><svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>Product image</div>`}
          ${p.tag ? `<span class="product-tag">${p.tag}</span>` : ''}
        </div>
        <div class="product-info">
          <h3 class="product-name">${p.name}</h3>
          <p class="product-desc">${p.description || ''}</p>
          <div class="product-footer">
            <span class="product-price">${formatPrice(p.price)}</span>
            <button class="btn-add"
              data-id="${p._id}"
              data-name="${p.name}"
              data-price="${p.price}"
              data-img="${p.imageUrl || ''}"
              ${p.stockQty === 0 ? 'disabled style="opacity:0.5;cursor:not-allowed;"' : ''}>
              ${p.stockQty === 0 ? 'Out of stock' : 'Add to bag'}
            </button>
          </div>
        </div>
      </article>`).join('');

    // Attach add-to-bag listeners on newly rendered cards
    grid.querySelectorAll('.btn-add').forEach(btn => {
      if (btn.disabled) return;
      btn.addEventListener('click', () => {
        const name = btn.dataset.name, price = Number(btn.dataset.price);
        const id = btn.dataset.id, imageUrl = btn.dataset.img || null;
        const existing = cart.find(i => i.id === id);
        if (existing) existing.qty += 1;
        else cart.push({ id, name, price, qty: 1, imageUrl });
        saveCart(); renderCart(); openCart();
        showToast(`${name} added to bag`, 'success');
      });
    });

    // Re-observe new cards for reveal animation
    grid.querySelectorAll('[data-reveal]').forEach(el => revealObserver.observe(el));
  } catch (err) {
    console.warn('API unavailable — showing static products. Error:', err.message);
    // Keep the static HTML cards if API is down
  }
}

/* ── Toast ── */
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span class="toast-dot"></span>${message}`;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3200);
}

/* ── Init ── */
renderCart();
loadProducts();
