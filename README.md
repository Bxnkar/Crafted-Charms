# Crafted Charms 🪷

**Handmade Jewellery from Mumbai** — A full-stack project combining a static web storefront with a Java e-commerce management system.

---

## 📁 Project Structure

```
Crafted-Charms/
│
├── index.html                  ← Main storefront (web frontend entry point)
│
├── frontend/                   ← Customer-facing website
│   ├── pages/                  ← HTML pages (auth, product detail)
│   ├── css/                    ← Stylesheets
│   ├── js/                     ← JavaScript logic (cart, auth, product)
│   └── assets/images/          ← Product & hero images
│
├── admin/                      ← Admin web dashboard
│   ├── pages/                  ← Admin login & dashboard HTML
│   ├── css/                    ← Admin styles
│   └── js/                     ← Admin logic
│
├── java-app/                   ← ★ Java Backend Management System
│   ├── pom.xml                 ← Maven build file
│   ├── README.md               ← Java app quick-start guide
│   └── src/
│       ├── main/java/com/craftedcharms/
│       │   ├── Main.java
│       │   ├── db/             ← DatabaseConnection (Singleton)
│       │   ├── model/          ← Entity classes (OOP hierarchy)
│       │   ├── dao/            ← Interfaces + JDBC implementations
│       │   ├── service/        ← Business logic layer
│       │   ├── ui/             ← Console menus (Presentation layer)
│       │   └── util/           ← Input validation, console colors
│       └── test/               ← JUnit 5 unit tests
│
├── sql/
│   ├── schema.sql              ← Database creation script
│   └── sample_data.sql         ← Seed data (15 products, demo users)
│
└── docs/
    ├── class_diagram.md        ← UML class hierarchy + OOP mapping
    ├── db_schema.md            ← Database table reference
    ├── requirement_coverage.md ← Assignment requirement checklist audit
    └── security_audit.md       ← Vulnerability report + mitigations
```

---

## 🚀 Getting Started

### Web Frontend
Open `index.html` in your browser.

```bash
npx serve .   # optional local dev server
```

### Java Application
See [java-app/README.md](java-app/README.md) for full instructions.

```bash
# 1. Create DB
mysql -u root -p < sql/schema.sql
mysql -u root -p crafted_charms < sql/sample_data.sql

# 2. Run
cd java-app
mvn exec:java
```

**Demo login:** `admin` / `admin123`

---

## ✨ Features

### Web Frontend
- **Storefront** — Hero section, featured products, custom order CTA
- **Product Detail** — Dynamic pages with image, description, stock status
- **Shopping Cart** — Slide-out cart drawer with localStorage persistence
- **Authentication** — Sign in / Sign up with form validation

### Java Application
- **Role-Based Access Control** — Admin & Customer portals
- **Product CRUD** — Add, view, update, deactivate, restock jewelry
- **Customer CRUD** — Full profile management with search
- **Order Management** — Cart → Checkout → Order lifecycle tracking
- **Secure Login** — PBKDF2 password hashing (legacy SHA-256 auto-migration)
- **Layered Architecture** — Presentation → Service → DAO → MySQL

---

## 🔷 OOP Concepts Demonstrated

| Concept            | Implementation                                               |
|--------------------|--------------------------------------------------------------|
| Abstract Class     | `Person`, `Product`                                          |
| Inheritance        | `Person→User→AdminUser/CustomerUser`, `Product→JewelryProduct→Ring/Necklace/Bracelet` |
| Interface          | `ProductDAO`, `CustomerDAO`, `UserDAO`, `OrderDAO`           |
| Method Overriding  | `getDetails()`, `calculateDiscount()`, `toString()`          |
| Method Overloading | `ProductService.search()` — 3 signatures                     |
| Encapsulation      | All fields `private` + getters/setters                       |
| Polymorphism       | Product subtypes via abstract `calculateDiscount()`          |
| Collections        | `ArrayList<CartItem>`, `HashMap<String, List<CartItem>>`     |
| JDBC               | 4 DAO implementations using `PreparedStatement`              |

---

## 🛠 Tech Stack

| Layer     | Technology                |
|-----------|---------------------------|
| Language  | Java 17                   |
| Database  | MySQL 8.x                 |
| JDBC      | mysql-connector-java 8.0  |
| Build     | Maven 3.x                 |
| Tests     | JUnit 5                   |
| Frontend  | Vanilla HTML, CSS, JS     |
| Fonts     | Cormorant Garamond + Inter|

