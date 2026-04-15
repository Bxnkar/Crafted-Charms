// ============================================================
//  Crafted Charms — Express + MongoDB Atlas API Server
//  Routes: /api/products  /api/users  /api/orders  /api/auth
// ============================================================
require('dotenv').config();
const express   = require('express');
const cors      = require('cors');
const mongoose  = require('mongoose');
const bcrypt    = require('bcryptjs');
const jwt       = require('jsonwebtoken');
const { Product, User, Order } = require('./models');

const app  = express();
const PORT = process.env.PORT || 5000;

// ── Middleware ────────────────────────────────────────────────
app.use(cors({ origin: process.env.CORS_ORIGIN || '*' }));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));

// ── MongoDB Atlas connection ──────────────────────────────────
mongoose
  .connect(process.env.MONGODB_URI)
  .then(() => console.log('✅ Connected to MongoDB Atlas — crafted_charms'))
  .catch(err => { console.error('❌ MongoDB connection failed:', err.message); process.exit(1); });

// ── JWT Auth Middleware ───────────────────────────────────────
function auth(req, res, next) {
  const header = req.headers.authorization;
  if (!header || !header.startsWith('Bearer '))
    return res.status(401).json({ error: 'No token provided' });
  try {
    req.user = jwt.verify(header.slice(7), process.env.JWT_SECRET);
    next();
  } catch {
    return res.status(401).json({ error: 'Invalid or expired token' });
  }
}

function adminOnly(req, res, next) {
  if (req.user?.role !== 'ADMIN')
    return res.status(403).json({ error: 'Admin access required' });
  next();
}

// ── Health check ──────────────────────────────────────────────
app.get('/api/health', (req, res) =>
  res.json({ status: 'ok', message: 'Crafted Charms API running' })
);

// ═══════════════════════════════════════════════════════════════
//  AUTH ROUTES
// ═══════════════════════════════════════════════════════════════

// POST /api/auth/register
app.post('/api/auth/register', async (req, res) => {
  try {
    const { fullName, username, email, password, phone, address, city } = req.body;
    if (!fullName || !username || !email || !password)
      return res.status(400).json({ error: 'fullName, username, email and password are required' });

    if (await User.findOne({ $or: [{ username }, { email }] }))
      return res.status(409).json({ error: 'Username or email already registered' });

    const passwordHash = await bcrypt.hash(password, 10);
    const user = await User.create({ fullName, username, email, passwordHash, phone, address, city });
    const token = jwt.sign({ id: user._id, role: user.role }, process.env.JWT_SECRET, { expiresIn: '7d' });

    res.status(201).json({
      token,
      user: { id: user._id, fullName: user.fullName, username: user.username, role: user.role }
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST /api/auth/login
app.post('/api/auth/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    if (!username || !password)
      return res.status(400).json({ error: 'Username and password required' });

    const user = await User.findOne({ username: username.toLowerCase() });
    if (!user || !(await bcrypt.compare(password, user.passwordHash)))
      return res.status(401).json({ error: 'Invalid credentials' });

    const token = jwt.sign({ id: user._id, role: user.role }, process.env.JWT_SECRET, { expiresIn: '7d' });
    res.json({
      token,
      user: { id: user._id, fullName: user.fullName, username: user.username, role: user.role }
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET /api/auth/me
app.get('/api/auth/me', auth, async (req, res) => {
  try {
    const user = await User.findById(req.user.id).select('-passwordHash');
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════
//  PRODUCT ROUTES
// ═══════════════════════════════════════════════════════════════

// GET /api/products  — public (all active products)
app.get('/api/products', async (req, res) => {
  try {
    const { search, category, maxPrice } = req.query;
    const filter = { isActive: true };
    if (category)  filter.category = category.toUpperCase();
    if (search)    filter.name     = { $regex: search, $options: 'i' };
    if (maxPrice)  filter.price    = { $lte: Number(maxPrice) };
    const products = await Product.find(filter).sort({ createdAt: -1 });
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET /api/products/all  — admin: includes inactive
app.get('/api/products/all', auth, adminOnly, async (req, res) => {
  try {
    const products = await Product.find().sort({ createdAt: -1 });
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET /api/products/:id
app.get('/api/products/:id', async (req, res) => {
  try {
    const product = await Product.findById(req.params.id);
    if (!product) return res.status(404).json({ error: 'Product not found' });
    res.json(product);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST /api/products  — admin only
app.post('/api/products', auth, adminOnly, async (req, res) => {
  try {
    const product = await Product.create(req.body);
    res.status(201).json(product);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// PUT /api/products/:id  — admin only
app.put('/api/products/:id', auth, adminOnly, async (req, res) => {
  try {
    const product = await Product.findByIdAndUpdate(req.params.id, req.body, { new: true, runValidators: true });
    if (!product) return res.status(404).json({ error: 'Product not found' });
    res.json(product);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// DELETE /api/products/:id  — admin: soft delete
app.delete('/api/products/:id', auth, adminOnly, async (req, res) => {
  try {
    const product = await Product.findByIdAndUpdate(req.params.id, { isActive: false }, { new: true });
    if (!product) return res.status(404).json({ error: 'Product not found' });
    res.json({ message: 'Product deactivated', product });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════
//  ORDER ROUTES
// ═══════════════════════════════════════════════════════════════

// POST /api/orders  — authenticated customer
app.post('/api/orders', auth, async (req, res) => {
  try {
    const { items, shippingAddress, paymentMethod } = req.body;
    if (!items || items.length === 0)
      return res.status(400).json({ error: 'Order must have at least one item' });

    // Validate stock & compute totals
    let totalAmount = 0;
    const resolvedItems = [];
    for (const item of items) {
      const product = await Product.findById(item.productId);
      if (!product || !product.isActive)
        return res.status(400).json({ error: `Product "${item.productName}" is unavailable` });
      if (product.stockQty < item.quantity)
        return res.status(400).json({ error: `Insufficient stock for "${product.name}"` });

      const subtotal = product.price * item.quantity;
      totalAmount   += subtotal;
      resolvedItems.push({
        product:     product._id,
        productName: product.name,
        quantity:    item.quantity,
        unitPrice:   product.price,
        subtotal,
        imageUrl:    product.imageUrl
      });

      // Decrement stock
      await Product.findByIdAndUpdate(product._id, { $inc: { stockQty: -item.quantity } });
    }

    const order = await Order.create({
      customer: req.user.id,
      items: resolvedItems,
      totalAmount,
      shippingAddress,
      paymentMethod
    });

    res.status(201).json(order);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET /api/orders  — admin: all orders
app.get('/api/orders', auth, adminOnly, async (req, res) => {
  try {
    const orders = await Order.find()
      .populate('customer', 'fullName email')
      .sort({ orderDate: -1 });
    res.json(orders);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// GET /api/orders/my  — customer: own orders
app.get('/api/orders/my', auth, async (req, res) => {
  try {
    const orders = await Order.find({ customer: req.user.id }).sort({ orderDate: -1 });
    res.json(orders);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// PATCH /api/orders/:id/status  — admin only
app.patch('/api/orders/:id/status', auth, adminOnly, async (req, res) => {
  try {
    const { status } = req.body;
    const order = await Order.findByIdAndUpdate(req.params.id, { status }, { new: true });
    if (!order) return res.status(404).json({ error: 'Order not found' });
    res.json(order);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// ═══════════════════════════════════════════════════════════════
//  USER ROUTES  (admin only)
// ═══════════════════════════════════════════════════════════════

// GET /api/users
app.get('/api/users', auth, adminOnly, async (req, res) => {
  try {
    const users = await User.find().select('-passwordHash').sort({ createdAt: -1 });
    res.json(users);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// POST /api/users
app.post('/api/users', auth, adminOnly, async (req, res) => {
  try {
    const { fullName, username, email, password, role = 'CUSTOMER', phone, address, city } = req.body;
    if (!fullName || !username || !email || !password)
      return res.status(400).json({ error: 'fullName, username, email and password are required' });

    if (await User.findOne({ $or: [{ username: username.toLowerCase() }, { email: email.toLowerCase() }] }))
      return res.status(409).json({ error: 'Username or email already registered' });

    const passwordHash = await bcrypt.hash(password, 10);
    const user = await User.create({
      fullName,
      username: username.toLowerCase(),
      email: email.toLowerCase(),
      passwordHash,
      role: role === 'ADMIN' ? 'ADMIN' : 'CUSTOMER',
      phone,
      address,
      city
    });

    res.status(201).json({
      id: user._id,
      _id: user._id,
      fullName: user.fullName,
      username: user.username,
      email: user.email,
      role: user.role,
      createdAt: user.createdAt
    });
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// PUT /api/users/:id
app.put('/api/users/:id', auth, adminOnly, async (req, res) => {
  try {
    const { passwordHash, ...updates } = req.body;  // prevent password override via this route
    const user = await User.findByIdAndUpdate(req.params.id, updates, { new: true }).select('-passwordHash');
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json(user);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

// DELETE /api/users/:id
app.delete('/api/users/:id', auth, adminOnly, async (req, res) => {
  try {
    await User.findByIdAndDelete(req.params.id);
    res.json({ message: 'User deleted' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ── 404 handler ───────────────────────────────────────────────
app.use((req, res) => res.status(404).json({ error: `Route ${req.path} not found` }));

// ── Start ─────────────────────────────────────────────────────
app.listen(PORT, () =>
  console.log(`🚀 Crafted Charms API → http://localhost:${PORT}/api/health`)
);
