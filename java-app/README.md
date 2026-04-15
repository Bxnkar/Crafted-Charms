# Crafted Charms — Java App

> Console-based Java management system for the Crafted Charms handmade jewellery brand.

## Quick Start

### 1. Set up MySQL

```bash
# Create DB and tables
mysql -u root -p < ../sql/schema.sql

# Seed demo data
mysql -u root -p crafted_charms < ../sql/sample_data.sql
```

### 2. Configure DB credentials

Use one of these options:

Option A (recommended): environment variables

```bash
export DB_URL="jdbc:mysql://localhost:3306/crafted_charms?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_USER="your_mysql_user"
export DB_PASSWORD="your_mysql_password"
```

Option B: local config file

```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

Then edit `src/main/resources/config.properties` with your local values.

### 3. Run the application

```bash
# Option A — Maven exec (recommended)
mvn exec:java

# Option B — Build and run JAR
mvn package
java -jar target/crafted-charms.jar
```

### 4. Run unit tests

```bash
mvn test
```

## Demo Login Credentials

| Role     | Username | Password   |
|----------|----------|------------|
| Admin    | admin    | admin123   |
| Customer | priya    | password   |
| Customer | ananya   | password   |
| Customer | riya     | password   |

## Project Structure

```
java-app/
├── pom.xml
└── src/
    ├── main/java/com/craftedcharms/
    │   ├── Main.java                   ← Entry point
    │   ├── db/
    │   │   └── DatabaseConnection.java ← Singleton JDBC connection
    │   ├── model/                      ← All entity classes
    │   │   ├── Person.java             ← Abstract (Abstraction)
    │   │   ├── User.java               ← Inheritance from Person
    │   │   ├── AdminUser.java          ← Inheritance from User
    │   │   ├── CustomerUser.java       ← Inheritance from User
    │   │   ├── Customer.java           ← Inheritance from Person
    │   │   ├── Product.java            ← Abstract (Polymorphism)
    │   │   ├── JewelryProduct.java     ← Concrete Product
    │   │   ├── Ring.java               ← Subclass of JewelryProduct
    │   │   ├── Necklace.java           ← Subclass of JewelryProduct
    │   │   ├── Bracelet.java           ← Subclass of JewelryProduct
    │   │   ├── Order.java
    │   │   ├── OrderItem.java
    │   │   ├── CartItem.java
    │   │   ├── Role.java               ← Enum
    │   │   ├── Category.java           ← Enum
    │   │   └── OrderStatus.java        ← Enum
    │   ├── dao/                        ← Interfaces + JDBC implementations
    │   │   ├── ProductDAO.java         ← Interface
    │   │   ├── ProductDAOImpl.java
    │   │   ├── CustomerDAO.java        ← Interface
    │   │   ├── CustomerDAOImpl.java
    │   │   ├── UserDAO.java            ← Interface
    │   │   ├── UserDAOImpl.java
    │   │   ├── OrderDAO.java           ← Interface
    │   │   └── OrderDAOImpl.java
    │   ├── service/                    ← Business Logic Layer
    │   │   ├── AuthService.java        ← Login, RBAC, PBKDF2 hashing
    │   │   ├── ProductService.java     ← Overloaded search()
    │   │   ├── CustomerService.java
    │   │   └── OrderService.java       ← HashMap cart
    │   ├── ui/                         ← Presentation Layer
    │   │   ├── MainMenu.java
    │   │   ├── AdminMenu.java
    │   │   └── CustomerMenu.java
    │   └── util/
    │       ├── InputValidator.java
    │       ├── PasswordHasher.java
    │       └── ConsoleColors.java
    └── test/java/com/craftedcharms/service/
        └── ProductServiceTest.java     ← JUnit 5 tests
```

## Security Notes

- Passwords are now stored using PBKDF2 with per-user salt.
- Existing legacy SHA-256 hashes are still accepted and auto-migrated on successful login.
- Order placement now runs in a DB transaction to avoid partial order writes.
