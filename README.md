# LetsPlay

Java and Spring Boot project that implements 
- **user authentication**, 
- **product management**, 
- and role-based access (**admin vs regular users**).  

The project uses **MongoDB** as a database and **Spring Security** for authentication and authorization.

---

## 🚀 Technologies & Frameworks

- [Java 17+] or newer
- [Spring Boot](https://spring.io/projects/spring-boot)
    - spring-boot-starter-web (REST API)
    - spring-boot-starter-security (authentication & authorization)
    - spring-boot-starter-validation (input validation)
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb) (database access)
- [BCryptPasswordEncoder](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html) (password hashing)
- [Maven](https://maven.apache.org/) (build tool)

---

## 📂 Project structure

```bash
src/main/java/com/letsplay
├── controller        # REST controllers
│   ├── UserController.java
│   └── ProductController.java
├── dto               # Data Transfer Objects
│   ├── UserResponse.java
│   ├── ProductResponse.java
│   ├── AdminProductResponse.java
│   └── SignupRequest.java
├── model             # MongoDB entities
│   ├── User.java
│   └── Product.java
├── repository        # Database repositories
│   ├── UserRepository.java
│   └── ProductRepository.java
├── security          # Security configuration and helpers
│   ├── SecurityConfig.java
│   ├── SecurityUtils.java
│   ├── JwtUtil
│   ├── AuthController
│   └── JwtRequestFilter
├── service           # Service classes with business logic
│   ├── UserService.java
│   └── ProductService.java
├── exception         # Global exception handling
│   └── GlobalExceptionHandler.java
└── LetsPlayApplication.java   # Main application class
```

---

## 🔑 Features

### Users
- **Sign up** (`POST /auth/signup`)
- **Login** (`POST /auth/login`)
- **/users/me** → returns the current logged-in user (name + email only, no password/role/id)

### Admin
- A default admin account is automatically created when the app starts.
- Admin can see **productId** for all products.
- Admin can update or delete any product.

### Products
- **GET /products** → returns public product info (without productId).
- **GET /products/{id}** → returns details of a specific product.
- **POST /products** → creates a new product (userId is set automatically).
- **GET /my-products** → shows products of the logged-in user (including productId so they can update/delete).
- **PUT /products/{id}**, **DELETE /products/{id}** → users can update/delete their own products, admin can update/delete any product.

---

## ⚙️ How to run

1. **Clone the repository**
   ```bash
   git clone https://github.com/karusmari/letsplay.git
   cd letsplay

2. **Set up MongoDB**
   - spring.data.mongodb.uri=mongodb://localhost:27017/letsplay
   - spring.data.mongodb.database=letsplay

3. **Build and run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   Or run LetsPlayApplication from IntelliJ.

4. **Test the API**
   - Use Postman.

   **Example signup:**
   - POST
   - https://localhost:8443/api/auth/signup
   - Content-Type: application/json

   ```bash
    {
       "name": "Maria",
       "email": "maria©example.com", 
       "password": "123"
    }
   ```
     
    **Example login:**
    - POST
    - https://localhost:8443/api/auth/login
    - Content-Type: application/json
   
    ```bash
     {
        "email": "maria©example.com", 
        "password": "123"
     }
    ```
   
    Login and use the returned JWT token for further requests.

      **Example create product:**
      - POST
      - https://localhost:8443/products
      - Content-Type: application/json
      - Authorization: Bearer <JWT_TOKEN
   
    ```bash
    {
         "name": "shoes",
         "description": "Nice shoes"
         "price": 10.0
    }
    ```

5. **Default admin account**
   - Email: admin@admin.com
   - Password: 123

    
6. **Using the database** (viewing, deleting)
    - Terminal: `mongosh`
    - Switch to the database: `use database`
    - Show collections: `show collections`
    - View users: `db.users.find().pretty()`
    - View products: `db.products.find().pretty()`
    - Delete user: `db.users.deleteOne({email: "maria@example.com"})`
    - Delete product: `db.products.deleteOne({name: "shoes"})`
    - Delete all products: `db.products.deleteMany({})`
    - Delete all users: `db.users.deleteMany({})`
    - Exit: `exit` or `quit`
   
