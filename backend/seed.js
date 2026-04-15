// ============================================================
//  Crafted Charms — MongoDB Seed Script
//  Run once: node seed.js
//  Seeds the crafted_charms database with products + admin user
// ============================================================
require('dotenv').config();
const mongoose = require('mongoose');
const bcrypt   = require('bcryptjs');
const { Product, User } = require('./models');

async function seed() {
  await mongoose.connect(process.env.MONGODB_URI);
  console.log('✅ Connected to MongoDB Atlas');

  // ── Clear existing data ──────────────────────────────────────
  await Product.deleteMany({});
  await User.deleteMany({});
  console.log('🗑  Collections cleared');

  // ── Admin user ───────────────────────────────────────────────
  const adminHash = await bcrypt.hash('admin123', 10);
  await User.create({
    fullName:     'Admin User',
    username:     'admin',
    email:        'admin@craftedcharms.in',
    passwordHash: adminHash,
    role:         'ADMIN'
  });

  // ── Sample customers ─────────────────────────────────────────
  const custHash = await bcrypt.hash('password', 10);
  await User.insertMany([
    { fullName:'Priya Sharma', username:'priya', email:'priya.sharma@gmail.com', passwordHash:custHash, role:'CUSTOMER', phone:'9876543210', address:'12 Marine Drive', city:'Mumbai' },
    { fullName:'Ananya Mehta', username:'ananya', email:'ananya.mehta@yahoo.com', passwordHash:custHash, role:'CUSTOMER', phone:'9988776655', address:'45 Bandra West',  city:'Mumbai' },
    { fullName:'Riya Joshi',   username:'riya',   email:'riya.joshi@gmail.com',   passwordHash:custHash, role:'CUSTOMER', phone:'9123456789', address:'78 Koregaon Park', city:'Pune'   },
  ]);
  console.log('👤 4 users seeded (admin + 3 customers)');

  // ── Products ─────────────────────────────────────────────────
  await Product.insertMany([
    { name:'Lotus Bloom Silver Ring',    category:'RING',     material:'Sterling Silver',  price:1499, stockQty:25, tag:'New',        description:'Delicate lotus flower hand-carved in 925 sterling silver.' },
    { name:'Golden Glow Kundan Ring',    category:'RING',     material:'Gold Plated',      price:3299, stockQty:15, tag:'Bestseller', description:'Traditional Kundan ring with intricate gold filigree and enamel accents.', gemstone:'Kundan' },
    { name:'Moonstone Dreams Ring',      category:'RING',     material:'Sterling Silver',  price:2199, stockQty:20, description:'Minimalist band with an iridescent moonstone centrepiece.', gemstone:'Moonstone' },
    { name:'Rose Quartz Love Ring',      category:'RING',     material:'Gold Filled',      price:1799, stockQty:30, description:'Heart-shaped rose quartz set in a gold-filled band.', gemstone:'Rose Quartz' },
    { name:'Peacock Feather Necklace',   category:'NECKLACE', material:'Sterling Silver',  price:2499, stockQty:18, tag:'Bestseller', description:'Hand-painted enamel peacock feather pendant on an 18-inch sterling chain.', gemstone:'Enamel' },
    { name:'Amethyst Garden Necklace',   category:'NECKLACE', material:'Oxidized Silver',  price:3799, stockQty:12, description:'Cluster of genuine amethyst stones set in oxidised silver.', gemstone:'Amethyst' },
    { name:'Boho Layered Necklace Set',  category:'NECKLACE', material:'Gold Plated',      price:1999, stockQty:22, description:'Set of 3 gold-plated layered chains with mixed charm pendants.' },
    { name:'Temple Gold Necklace',       category:'NECKLACE', material:'Gold Plated',      price:5999, stockQty: 8, description:'Handcrafted temple-style gold necklace with ruby accents.', gemstone:'Ruby' },
    { name:'Twisted Silver Bracelet',    category:'BRACELET', material:'Sterling Silver',  price:1299, stockQty:35, description:'Simple twisted-wire design in pure 925 sterling silver — adjustable.' },
    { name:'Turquoise Beaded Bracelet',  category:'BRACELET', material:'Natural Stone',    price: 899, stockQty:40, tag:'New',        description:'Natural turquoise beads strung on elastic for a casual style.', gemstone:'Turquoise' },
    { name:'Gold Charm Bracelet',        category:'BRACELET', material:'Gold Plated',      price:2799, stockQty:16, description:'Delicate gold-plated chain with 5 handcrafted Mumbai-inspired charms.' },
    { name:'Labradorite Wrap Bracelet',  category:'BRACELET', material:'Sterling Silver',  price:3299, stockQty:10, description:'Multi-strand iridescent labradorite stone wrap-around cuff.', gemstone:'Labradorite' },
    { name:'Classic Jhumka Gold',        category:'EARRING',  material:'Gold Plated',      price:1599, stockQty:28, tag:'Bestseller', description:'Traditional bell-shaped jhumka earrings in high-quality gold plating.' },
    { name:'Pearl Drop Earrings',        category:'EARRING',  material:'Sterling Silver',  price:2299, stockQty:20, description:'Freshwater pearl drop earrings with a sterling silver setting.', gemstone:'Pearl' },
    { name:'Oxidised Tribal Earrings',   category:'EARRING',  material:'Oxidized Silver',  price: 999, stockQty:33, description:'Large tribal-inspired hoop earrings in oxidised silver.' },
  ]);
  console.log('💍 15 products seeded');

  console.log('\n✅ Seed complete!\n');
  console.log('  Admin login:    username=admin    password=admin123');
  console.log('  Customer login: username=priya    password=password\n');
  await mongoose.disconnect();
}

seed().catch(err => { console.error(err); process.exit(1); });
