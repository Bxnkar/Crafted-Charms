// ============================================================
//  Crafted Charms — MongoDB Schemas
//  All 3 collections: products, orders, users
// ============================================================
const mongoose = require('mongoose');

// ── Product ──────────────────────────────────────────────────
const productSchema = new mongoose.Schema({
  name:        { type: String, required: true, trim: true },
  description: { type: String, trim: true },
  category:    { type: String, required: true, enum: ['RING','NECKLACE','BRACELET','EARRING'] },
  material:    { type: String, required: true, trim: true },
  gemstone:    { type: String, trim: true },
  price:       { type: Number, required: true, min: 0 },
  stockQty:    { type: Number, required: true, default: 0, min: 0 },
  isActive:    { type: Boolean, default: true },
  tag:         { type: String, trim: true },           // e.g. "Bestseller"
  imageUrl:    { type: String },                        // admin-uploaded image URL
  createdAt:   { type: Date, default: Date.now }
});

// ── User ─────────────────────────────────────────────────────
const userSchema = new mongoose.Schema({
  fullName:     { type: String, required: true, trim: true },
  username:     { type: String, required: true, unique: true, trim: true, lowercase: true },
  email:        { type: String, required: true, unique: true, trim: true, lowercase: true },
  passwordHash: { type: String, required: true },       // bcrypt hash
  role:         { type: String, enum: ['ADMIN','CUSTOMER'], default: 'CUSTOMER' },
  phone:        { type: String },
  address:      { type: String },
  city:         { type: String },
  loyaltyPoints:{ type: Number, default: 0 },
  createdAt:    { type: Date, default: Date.now }
});

// ── Order ─────────────────────────────────────────────────────
const orderItemSchema = new mongoose.Schema({
  product:     { type: mongoose.Schema.Types.ObjectId, ref: 'Product' },
  productName: { type: String, required: true },        // snapshot at purchase time
  quantity:    { type: Number, required: true, min: 1 },
  unitPrice:   { type: Number, required: true, min: 0 },
  subtotal:    { type: Number, required: true, min: 0 },
  imageUrl:    { type: String }
}, { _id: false });

const orderSchema = new mongoose.Schema({
  customer:        { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  items:           [orderItemSchema],
  totalAmount:     { type: Number, required: true, min: 0 },
  status:          { type: String, enum: ['PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED'], default: 'PENDING' },
  shippingAddress: { type: String, required: true },
  paymentMethod:   { type: String, required: true },
  orderDate:       { type: Date, default: Date.now }
});

module.exports = {
  Product: mongoose.model('Product', productSchema),
  User:    mongoose.model('User',    userSchema),
  Order:   mongoose.model('Order',   orderSchema),
};
