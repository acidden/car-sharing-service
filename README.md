# Car Sharing Service 🚗💨

An automated online management system for a city car sharing service. This project modernizes an outdated paper-based tracking system into a high-performance REST API backend, streamlining work for administrators (Managers) and delivering a seamless rental experience for Customers.

---

## 🌟 Key Features

* **Car Inventory Management**: Comprehensive CRUD operations with real-time tracking of available cars, automatic stock decrements upon booking, and increments upon return.
* **User Authentication**: Secure user registration and authentication powered by **JWT (JSON Web Tokens)** with role-based access control (`MANAGER` and `CUSTOMER`).
* **Rental System**: Seamless rental creation with validation checks (e.g., preventing car rental if stock is 0 or if the user has an outstanding payment).
* **Automated Payments (Stripe)**: Integration with the Stripe API for handling security deposits and overdue fines using a secure checkout session redirection.
* **Telegram Notification Service**: Instant automated alerts sent via a Telegram Bot to managers for new rentals, successful payments, and a daily automated cron-job to flag overdue rentals.

---

## 🛠️ Tech Stack & Architecture

* **Backend Framework**: Java 21 / Spring Boot 4
* **Database**: MySQL (Production/Development)
* **Database Migration**: Liquibase
* **Security**: Spring Security & JWT
* **Integrations**: Stripe Java SDK (Payments), Telegram Bot API (Notifications)
* **Testing**: JUnit 5, H2 Database (In-Memory with MySQL compatibility mode)
* **Code Quality**: Maven Checkstyle Plugin
* **DevOps & Containerization**: Docker, Docker Compose, GitHub Actions (CI)

---

## 🚀 Getting Started & Local Launch

### Prerequisites
Make sure you have the following installed on your machine:
* **Docker** & **Docker Compose**
* **Java 21 SDK** (if running locally without Docker)
* **Maven**

### Environment Configuration
1. Duplicate the `.env.sample` template file located in the root directory:
   ```bash
   cp .env.sample .env
   ```
2. Open the newly created `.env` file and fill in your actual private configuration variables (Stripe keys, Telegram Token, Database credentials). *Note: The `.env` file is ignored by Git to prevent leaking sensitive keys.*

### Running the Application with Docker

To build and run the entire ecosystem (Spring Boot App + MySQL DB) in containers, simply execute:

```bash
docker-compose up --build
```
Once the containers are up and running, the backend web service will be accessible at `http://localhost:8080`.

---

## 📖 API Documentation & Swagger

The project is fully documented using OpenAPI / Swagger UI specifications. You can easily view, test, and explore all available endpoints (including authentication, car inventory, rentals, and payments) by navigating to:

➡️ **`http://localhost:8080/swagger-ui/index.html`**

*Note: Endpoints and tags inside the Swagger UI are sorted alphabetically (`operationsSorter: alpha`) for better readability and faster navigation.*

---

## 👥 Controllers & Endpoints Overview

* **Authentication (`/register`, `/login`)**: Registration and JWT token acquisition.
* **Users (`/users`)**: Profile management (`/me`) and user role updates for management.
* **Cars (`/cars`)**: Full CRUD functionality. Accessible to all for browsing; restricted to `MANAGER` for additions/modifications.
* **Rentals (`/rentals`)**: Creating rentals, filtering active/history records, and processing vehicle returns.
* **Payments (`/payments`)**: Generating payment sessions, handling success hooks, and managing overdue fines calculated via a preconfigured `2.0x` fine multiplier.

---

## 📈 Quality Assurance & CI/CD

* **Automated Tests**: Main services and controllers are well-covered with unit and integration tests. An isolated **In-Memory H2 Database** running in MySQL compatibility mode is utilized during the test phase to guarantee fast execution and clean state isolation.
* **Running Tests Locally**:
  ```bash
  mvn clean test
  ```
* **Continuous Integration**: GitHub Actions configuration (`.github/workflows/ci.yml`) automatically executes `mvn clean verify` and checkstyle validation on every Pull Request before code can be safely merged into `master`.
