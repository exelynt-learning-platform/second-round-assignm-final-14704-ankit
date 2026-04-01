# E-Commerce Backend System

A comprehensive Spring Boot backend for an e-commerce platform with JWT authentication, product management, shopping cart, order processing, and Stripe payment integration.

## Features

### Authentication & Authorization
- User registration and login with JWT token-based authentication
- Password encryption using BCrypt
- Role-based access control (ROLE_USER, ROLE_ADMIN)
- Secure JWT token validation and expiration management

### Product Management
- Full CRUD operations for products
- Product attributes: name, description, price, stock quantity, image URL
- Stock quantity validation
- Admin-only product management

### Shopping Cart
- Add products to cart with quantity validation
- Update cart items
- Remove items from cart
- Clear entire cart
- Automatic stock verification
- Cart persistence per user

### Order Management
- Create orders from cart items
- View order details
- Track order status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- Automatic stock reduction on order creation
- Automatic cart clearing after order placement

### Payment Processing
- Stripe payment integration
- Create payment intents
- Confirm payments
- Handle payment success/failure
- Process refunds

### Error Handling & Validation
- Comprehensive input validation
- Custom exception handling
- Meaningful error messages
- HTTP status code compliance

## Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: H2 (in-memory for testing), MySQL ready
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT (jjwt)
- **Payment**: Stripe SDK
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito

## Quick Start - Testing with Swagger UI

The API includes interactive Swagger documentation for testing all endpoints:

1. **Start the application**:
   ```bash
   mvn spring-boot:run
   ```

2. **Access Swagger UI** at: `http://localhost:8080/api/swagger-ui.html`

3. **Test API endpoints** directly in Swagger UI:
   - Register/Login to get JWT token
   - Click 🔒 Authorize button to set the token
   - Try out all endpoints with interactive forms

📖 **See [SWAGGER_GUIDE.md](./SWAGGER_GUIDE.md)** for detailed Swagger testing instructions and API workflow examples.

## Project Structure

```
src/main/java/com/ecommerce/
├── config/
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller/
│   ├── AuthController.java
│   ├── CartController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   └── ProductController.java
├── dto/
│   ├── AddToCartRequest.java
│   ├── AuthResponse.java
│   ├── CartDTO.java
│   ├── CartItemDTO.java
│   ├── CreateOrderRequest.java
│   ├── LoginRequest.java
│   ├── OrderDTO.java
│   ├── OrderItemDTO.java
│   ├── PaymentRequest.java
│   ├── PaymentResponse.java
│   ├── ProductDTO.java
│   └── RegisterRequest.java
├── exception/
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   ├── InvalidRequestException.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
├── model/
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Payment.java
│   ├── Product.java
│   └── User.java
├── repository/
│   ├── CartItemRepository.java
│   ├── CartRepository.java
│   ├── OrderRepository.java
│   ├── PaymentRepository.java
│   ├── ProductRepository.java
│   └── UserRepository.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
├── service/
│   ├── AuthService.java
│   ├── CartService.java
│   ├── OrderService.java
│   ├── PaymentService.java
│   └── ProductService.java
└── EcommerceApplication.java

src/test/java/com/ecommerce/
├── security/
│   └── JwtTokenProviderTest.java
└── service/
    ├── AuthServiceTest.java
    ├── CartServiceTest.java
    ├── OrderServiceTest.java
    └── ProductServiceTest.java
```

## Database Schema

### Users Table
- id (PK)
- email (unique)
- firstName
- lastName
- password (encrypted)
- roles (element collection)
- status
- createdAt
- updatedAt

### Products Table
- id (PK)
- name
- description
- price
- stockQuantity
- imageUrl
- status
- createdAt
- updatedAt

### Carts Table
- id (PK)
- user_id (FK, unique)
- createdAt
- updatedAt

### CartItems Table
- id (PK)
- cart_id (FK)
- quantity
- createdAt
- updatedAt

### Orders Table
- id (PK)
- user_id (FK)
- totalPrice
- orderStatus
- paymentStatus
- shippingAddress
- shippingCity
- shippingState
- shippingZipCode
- shippingCountry
- paymentMethod
- stripePaymentIntentId
- createdAt
- updatedAt

### OrderItems Table
- id (PK)
- order_id (FK)
- quantity
- pricePerUnit
- createdAt
- updatedAt

### Payments Table
- id (PK)
- order_id (FK, unique)
- amount
- paymentMethod
- status
- stripePaymentIntentId
- transactionId
- failureReason
- createdAt
- updatedAt

## API Endpoints

### Authentication Endpoints

#### Register User
```
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "securePassword123"
}

Response: 201 Created
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login User
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}

Response: 200 OK
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Product Endpoints

#### Get All Products
```
GET /api/products
Response: 200 OK
[
  {
    "id": 1,
    "name": "Product Name",
    "description": "Product Description",
    "price": 99.99,
    "stockQuantity": 100,
    "imageUrl": "http://example.com/image.jpg",
    "status": "AVAILABLE",
    "createdAt": 1704067200000,
    "updatedAt": null
  }
]
```

#### Get Product by ID
```
GET /api/products/{productId}
Response: 200 OK
{
  "id": 1,
  "name": "Product Name",
  "description": "Product Description",
  "price": 99.99,
  "stockQuantity": 100,
  "imageUrl": "http://example.com/image.jpg",
  "status": "AVAILABLE",
  "createdAt": 1704067200000,
  "updatedAt": null
}
```

#### Create Product (Admin Only)
```
POST /api/products
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "New Product",
  "description": "Product Description",
  "price": 49.99,
  "stockQuantity": 50,
  "imageUrl": "http://example.com/image.jpg"
}

Response: 201 Created
```

#### Update Product (Admin Only)
```
PUT /api/products/{productId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Updated Name",
  "price": 59.99
}

Response: 200 OK
```

#### Delete Product (Admin Only)
```
DELETE /api/products/{productId}
Authorization: Bearer {token}

Response: 204 No Content
```

### Cart Endpoints

#### Add to Cart
```
POST /api/cart/items
Authorization: Bearer {token}
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}

Response: 200 OK
```

#### Get Cart
```
GET /api/cart
Authorization: Bearer {token}

Response: 200 OK
{
  "id": 1,
  "cartItems": [
    {
      "id": 1,
      "products": [...],
      "quantity": 2,
      "totalPrice": 199.98,
      "createdAt": 1704067200000,
      "updatedAt": null
    }
  ],
  "totalPrice": 199.98,
  "createdAt": 1704067200000,
  "updatedAt": 1704067260000
}
```

#### Update Cart Item
```
PUT /api/cart/items/{cartItemId}?quantity=5
Authorization: Bearer {token}

Response: 200 OK
```

#### Remove from Cart
```
DELETE /api/cart/items/{cartItemId}
Authorization: Bearer {token}

Response: 200 OK
```

#### Clear Cart
```
DELETE /api/cart
Authorization: Bearer {token}

Response: 204 No Content
```

### Order Endpoints

#### Create Order
```
POST /api/orders
Authorization: Bearer {token}
Content-Type: application/json

{
  "shippingAddress": "123 Main Street",
  "shippingCity": "New York",
  "shippingState": "NY",
  "shippingZipCode": "10001",
  "shippingCountry": "USA",
  "paymentMethod": "STRIPE"
}

Response: 201 Created
{
  "id": 1,
  "userId": 1,
  "orderItems": [...],
  "totalPrice": 199.98,
  "orderStatus": "PENDING",
  "paymentStatus": "UNPAID",
  "shippingAddress": "123 Main Street",
  "shippingCity": "New York",
  "shippingState": "NY",
  "shippingZipCode": "10001",
  "shippingCountry": "USA",
  "paymentMethod": "STRIPE",
  "createdAt": 1704067200000,
  "updatedAt": null
}
```

#### Get Order by ID
```
GET /api/orders/{orderId}
Authorization: Bearer {token}

Response: 200 OK
```

#### Get User Orders
```
GET /api/orders
Authorization: Bearer {token}

Response: 200 OK
[...]
```

#### Update Order Status (Admin Only)
```
PUT /api/orders/{orderId}/status?status=CONFIRMED
Authorization: Bearer {token}

Response: 200 OK
```

### Payment Endpoints

#### Process Payment
```
POST /api/payments/process
Authorization: Bearer {token}
Content-Type: application/json

{
  "orderId": 1
}

Response: 200 OK
{
  "paymentId": 1,
  "orderId": 1,
  "status": "PENDING",
  "stripePaymentIntentId": "pi_1234567890"
}
```

#### Confirm Payment
```
POST /api/payments/confirm/{paymentIntentId}

Response: 200 OK
{
  "paymentId": 1,
  "orderId": 1,
  "status": "PAID",
  "transactionId": "ch_1234567890",
  "stripePaymentIntentId": "pi_1234567890"
}
```

#### Refund Payment (Admin Only)
```
POST /api/payments/{paymentId}/refund
Authorization: Bearer {token}

Response: 204 No Content
```

## Configuration

### Application Properties

The application is configured via `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: ecommerce-backend
  
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

app:
  jwt:
    secret: ${JWT_SECRET:YourSecureSecretKey}
    expiration: 86400000

stripe:
  api-key: ${STRIPE_API_KEY:sk_test_your_stripe_key}
```

### Environment Variables

Set the following environment variables:
- `JWT_SECRET`: Your JWT secret key (min 256 bits for HS256)
- `STRIPE_API_KEY`: Your Stripe API secret key

For production, use MySQL:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce
    username: root
    password: yourpassword
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## Building the Project

### Prerequisites
- Java 17 or higher
- Maven 3.8.0 or higher

### Build Steps

1. Clone the repository
```bash
cd "c:\Users\abcom\Multigenesys Assignment"
```

2. Build the project
```bash
mvn clean install
```

3. Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Running Tests

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=AuthServiceTest
```

Generate test coverage report:
```bash
mvn jacoco:report
```

## Security Considerations

1. **Password Storage**: All passwords are hashed using BCrypt with 10 rounds
2. **JWT Tokens**: Tokens expire after 24 hours (configurable)
3. **HTTPS**: Use HTTPS in production
4. **CORS**: Configure CORS appropriately for your frontend
5. **Rate Limiting**: Implement rate limiting for authentication endpoints
6. **SQL Injection**: Use parameterized queries via JPA
7. **CSRF Protection**: Disabled for stateless API (consider enabling for web forms)

## Error Handling

The API returns standard HTTP status codes:
- `200 OK`: Successful GET/PUT request
- `201 Created`: Successful POST request (resource created)
- `204 No Content`: Successful DELETE request
- `400 Bad Request`: Invalid input or validation error
- `401 Unauthorized`: Missing or invalid authentication token
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

Error Response Format:
```json
{
  "status": 400,
  "message": "Validation failed",
  "validationErrors": {
    "email": "Email is required",
    "password": "Password should be at least 8 characters"
  }
}
```

## Data Model Relationships

- **One-to-One**: User ↔ Cart
- **One-to-Many**: User → Orders, Cart → CartItems, Order → OrderItems
- **Many-to-Many**: CartItems ↔ Products, OrderItems ↔ Products

## Testing

The project includes comprehensive unit tests using JUnit 5 and Mockito:

### Test Coverage
- **AuthService**: Registration, login, user retrieval
- **ProductService**: CRUD operations, validation
- **CartService**: Adding, updating, removing items
- **OrderService**: Order creation, retrieval, status updates
- **JwtTokenProvider**: Token generation and validation

Run tests:
```bash
mvn test
```

## Future Enhancements

- Integration tests with TestContainers
- Category and subcategory management
- Product reviews and ratings
- Wishlist functionality
- Inventory management
- Email notifications
- SMS notifications
- User profile management
- Order tracking with notifications
- Admin dashboard
- Analytics and reporting
- Caching with Redis
- API documentation with Swagger/OpenAPI

## Troubleshooting

### Build Issues
- Clean Maven cache: `mvn clean`
- Update dependencies: `mvn dependency:resolve -Drefresh`

### Runtime Issues
- Check logs for detailed error messages
- Verify JWT_SECRET and STRIPE_API_KEY are set
- Ensure database is properly initialized

### Test Failures
- Run tests with verbose output: `mvn test -X`
- Check test database configuration

## License

This project is provided as-is for educational purposes.

## Contact & Support

For issues or questions, please review the code documentation or contact the development team.
