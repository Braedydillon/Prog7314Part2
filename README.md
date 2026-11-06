## Core purpose and scope of application:

Unify is a mass market retail mobile application that is developed for Android. Unify is designed with the thought of providing users with a platform for browsing products, managing a shopping cart and completing purchases.

The application makes use of a combination of Android front end with Firebase services and Spring Boot backend which is connected to a Neon PostgreSQL database. The structure allows the application to separate real-time user specific information from important transactional and product data.

The Application includes the following features:

- User registration and login
- Firebase Authentication
- Google Sign-In and biometric authentication
- Product browsing
- Product categories
- Product details
- Shopping cart management
- Wishlist management
- Order processing
- Order history
- User addresses
- Product barcode scanning
- Offline shopping cart using RoomDB
- Automatic cart synchronization when internet connectivity is restored
- Offline cart persistence and product removal
- Multilingual support (English, Afrikaans and isiZulu)
- Firebase Cloud Messaging (FCM) notifications
- Login-streak notifications
- Admin order management
- Order status updates and customer order tracking
- Real-time shipping notifications
- Role-based administrator access
- Wishlist management
- Product reviews and customer feedback

## Purpose:

The purpose of this application is to provide customers with an online mobile shopping experience where they can discover products, view product information, manage their shopping cart and complete orders using their mobile device.

This project was developed in demonstrating the development and integration of multiple technologies into a single solution.

## Intended Users:

Primary users of the application are retail customers.

Customers can:
- Create an account
- Log into the application
- Use Google Sign-In or biometric authentication
- Browse available products
- Search and discover products
- View product information
- Add products to their shopping cart
- Remove products from their cart
- Manage their wishlist
- Manage delivery addresses
- Place orders
- View previous orders
- Track order statuses
- Receive shipping and delivery notifications
- Scan product barcodes
- Access their shopping cart offline
- Change the application language

Administrators can:
- View customer orders
- Manage order statuses
- Update orders to Processing, Shipped and Delivered
- Trigger automatic customer notifications when order statuses change

## Application Scope:

There is a usage of three major components being:
- Android mobile application
- Firebase services
- Spring Boot REST API with Neon PostgreSQL

The Android application provides the interface and handles the user interaction.

Firebase provides authentication, user specific real-time data and push notifications through Firebase Cloud Messaging.

RoomDB is used to store shopping cart information locally on the device, allowing users to view their cart and remove products without an internet connection. When connectivity is restored, pending changes are synchronized with Firebase Firestore. Checkout requires an internet connection, and the local cart is cleared after a successful purchase.

Spring Boot backend provides the REST API used to access the product and data stored in PostgreSQL. It also manages order processing, administrative operations and order-status updates.

Neon PostgreSQL stores product information, order data and FCM device tokens.

## GitHub Actions:

GitHub Actions tests the back-end service whenever pushes or pull requests are made. This helps identify any issues before changes are merged.

## Structural and Architectural:

Unify uses a 3-part architectural system which consists of the Android front end, Firebase services and Spring Boot for the backend. This structure separates the responsibilities of each of the components, making the application easier to maintain and allowing each part of the system to perform its function.

## User Intended Features:

### Server-side validation
- Orders require valid items, addresses and products
- Stock and product prices are validated

### Registration and session management
- Firebase handles user registration and authentication
- Authenticated requests require a valid Firebase token
- Google Sign-In and biometric authentication are supported

### Infinite product scrolling
- Products load 20 per page
- Additional pages load as the user scrolls
- Products are randomised to reduce repetition

### Offline shopping cart and synchronization
- RoomDB stores shopping cart information locally
- Users can view and remove cached cart items while offline
- Pending changes synchronize with Firestore when internet connectivity returns
- Checkout is restricted while offline or when synchronization is pending

### Multilingual support
- Supports English, Afrikaans and isiZulu
- Users can change languages through Settings
- Translated resources are available for supported screens, including Login and Registration
- Some application messages and product information remain untranslated

### Firebase Cloud Messaging
- FCM provides push notifications
- Login-streak notifications were successfully tested
- Customers receive notifications when their order status changes
- Device tokens are registered and stored for notification delivery

### Admin order management
- Administrators can view customer orders
- Order statuses can be updated to Processing, Shipped and Delivered
- Customers can track their latest order statuses
- Backend role-based access control restricts administrative functionality

### Backend API and database integration
- Spring Boot REST API manages product retrieval, orders and administrative operations
- Neon PostgreSQL stores product, order and notification token information
- Backend endpoints support order-status updates and customer notifications

### User interface improvements
- Improved Settings language selector with a rounded dropdown
- Updated wishlist button appearance
- Added multilingual support to authentication screens
- Improved the overall user experience while maintaining existing functionality

## Security Features:

- Firebase Authentication protects user accounts
- Firebase tokens are required for authenticated backend requests
- Role-based access control restricts administrator functionality
- Server-side validation checks order information, stock and prices
- Biometric authentication provides an additional sign-in option
- Offline checkout restrictions help prevent orders using unsynchronized cart information

## Testing:

- Tested Firebase authentication functionality
- Tested offline cart persistence and synchronization using RoomDB and Firestore
- Tested language switching between English, Afrikaans and isiZulu
- Successfully tested Firebase Cloud Messaging notifications
- Confirmed delivery of login-streak notifications
- GitHub Actions performs automated backend checks

## Current Limitations:

- Some application messages and product information remain untranslated
- Dedicated Spring Boot synchronization endpoints have not been implemented
- Cross-device cart conflict resolution has not been implemented

## Setup and Installation Instructions

### Requirements
- Android Studio
- JDK 17
- Internet connection
- Firebase project configuration
- Access to the Neon PostgreSQL database

### Android Application Setup
1. Clone the GitHub repository.
2. Open the `android` folder in Android Studio.
3. Allow Gradle to synchronize the project.
4. Configure Firebase using the `google-services.json` file.
5. Ensure the application is configured with the correct Spring Boot API base URL.
6. Connect an Android device or start an emulator.
7. Build and run the application.

The Android application communicates with the Spring Boot backend through REST API endpoints to retrieve products, process orders and access other backend functionality. Firebase handles authentication and user-specific data.

### Spring Boot Backend Setup
1. Open the `backend/api` folder in IntelliJ IDEA or another compatible IDE.
2. Configure the required database environment variables using `backend/api/.env.example` as a reference:
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
3. Ensure the application can connect to the Neon PostgreSQL database.
4. Run `ApiApplication.kt` to start the Spring Boot backend.
5. Verify that the API is accessible.

### Backend Testing
Backend tests can be executed using the Gradle wrapper.

On Windows:

```bash
cd backend/api
gradlew.bat test
```

On Linux or macOS:

```bash
cd backend/api
./gradlew test
```

### GitHub Actions
The repository includes a GitHub Actions workflow that automatically executes backend tests when changes are pushed or pull requests affect the `backend/api` directory.

The workflow uses Java 17 and Gradle to execute the tests, helping identify errors before changes are merged.

## CI/CD Pipeline Configuration

GitHub Actions is used to automate backend testing and improve code reliability.

- The pipeline runs automatically when backend changes are pushed or pull requests are created.
- Java 17 and Gradle are used to execute backend tests.
- The workflow fails when tests are unsuccessful, helping identify issues before merging.
- GitHub Actions provides test results and execution logs for reviewing pipeline performance.

### Static Code Analysis

Static code analysis is required to identify potential security vulnerabilities and code quality issues. This functionality still needs to be verified or integrated into the pipeline.

Analysis results and any fixes applied will be documented as part of the final submission.


## Docker and Containerisation

The Spring Boot backend includes a Dockerfile to support containerised deployment.

Docker allows the backend to run in a consistent environment, reducing configuration differences between development and deployment.

Docker Compose integration and frontend containerisation are still in progress.

## Logging and Monitoring

The Spring Boot backend uses SLF4J logging to support application monitoring and debugging.

- Backend logs help track API activity and identify application issues.
- Logging assists developers in investigating errors and troubleshooting unexpected behaviour.
- Logs can be reviewed during development to monitor backend operations.
- Additional logging for authentication, orders and transactions will be verified as part of the final implementation.

Logging improves system reliability and helps identify potential security concerns.

## Security Features and Security Review

Unify Retail implements several security measures to protect user accounts, transactions and administrative functionality.

### Security Features

- **Firebase Authentication:** Secures user registration and login through authenticated accounts.
- **Biometric Authentication:** Provides an additional device-based login option.
- **Role-Based Access Control:** Restricts administrative order management to authorized administrators.
- **Server-Side Validation:** Validates products, stock quantities, prices and order information before processing.
- **Firebase Token Verification:** Protects backend endpoints by requiring valid authentication tokens.

### Security Review

The application addresses common security threats through the following measures:

- **Authentication Weaknesses:** Firebase Authentication and biometric login help protect account access.
- **Unauthorized Access:** Backend role-based permissions prevent regular customers from accessing administrator functionality.
- **Injection Attacks:** Spring Data JPA supports parameterized database access, reducing SQL injection risks when used correctly.
- **Cross-Site Scripting (XSS):** The native Android interface reduces exposure to traditional browser-based XSS, although any rendered web content must still be handled securely.
- **Transaction Manipulation:** Server-side validation helps prevent invalid product prices, stock quantities and order information from being submitted.

These security measures improve application integrity, protect sensitive operations and reduce the risk of unauthorized access.

### Additional Features — POE Part 3

**Wishlist Management**
- Users can add products to their wishlist.
- Users can view and remove saved products.
- The wishlist allows customers to save products they are interested in purchasing later.

**Product Reviews**
- Users can review products through the application.
- Product reviews allow customers to share feedback about products.
- Reviews help other customers make informed purchasing decisions.
