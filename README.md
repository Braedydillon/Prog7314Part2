Core purpose and scope of application:
Unify is a mass market retail mobile application that is developed for android. Unify is designed with the thought of providing users with a platform for browsing products, managing a shopping cart and completing purchases

The application makes use of a combination of android front end with firebase services and Spring Boot backend which is connected to a PostgreSQL database. The structure allows the application to separate real-time user specific information from important transactional and product data

The Application includes the following features:

- User registration and login
- Firebase Authentication
- Product browsing
- Product categories
- Product details
- Shopping cart management
- Order processing
- Order history
- User addresses
- Product barcode scanning
- Offline shopping cart using RoomDB
- Automatic cart synchronization when internet connectivity is restored
- Offline cart persistence and product removal


Purpose:
The purpose of this application is to provide customers with an online mobile shopping experience where they can discover products, view product information, manage their shopping cart and complete orders using their mobile device. 

This project was developed in demonstrating the development and integration of multiple technologies into a single solution.



Intended Users:
Primary users of the application are retail customers
Customers can:
- Create an account
- Log into the application
- Browse available products
- Search and discover products
- View product information
- Add products to their shopping cart
- Remove products from their cart
- Manage delivery addresses
- Place orders
- View previous orders
- Scan product barcodes

Application Scope:
There is a usage of three major components being:
- Android mobile application
- Firebase services 
- Spring Boot rest Api with PostgreSQL

The android application provides the interface and handles the user interaction.
Firebase provides authentication and user specific real time data
RoomDB is used to store shopping cart information locally on the device, allowing users to view their cart and remove products without an internet connection. When connectivity is restored, pending changes are synchronized with Firebase Firestore. Checkout requires an internet connection, and the local cart is cleared after a successful purchase.
Spring Boot backend provides the REST API used to access the product and data stored in the PostgreSQL
GitHub Actions:
GitHub actions test the back-end service whenever push or pull requests are made. This helps identify any issues before changes are merged

Structural and Architectural 
Unify uses a 3-part architectural system which consists of the android front end, firebase services and Spring Boot for the backend. This structure separates the responsibilities of each of the components, making the application easier to maintain and allows the specific part of the system to perform its function


User Intended Features:

Server-side validation 
- Orders require valid items, addresses and products 
- Stock and product prices are validated 

Registration and session management 
- Firebase handles user registration and authentication 
- Authenticated requests require a valid Firebase token 

 Infinite product scrolling 
- Products load 20 per page 
- Additional pages load as the user scrolls 
- Products are randomised to reduce repetition
