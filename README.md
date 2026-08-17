# Eclipse Login System

A comprehensive Java login system built with JavaFX, BCrypt password hashing, and support for both file-based (JSON) and SQLite database storage.

## Features

- **Dual Storage**: Switch between JSON file and SQLite database with one config change
- **Secure Authentication**: BCrypt password hashing with configurable cost factor
- **JavaFX GUI**: Professional login, registration, and password recovery screens
- **Input Validation**: Email format, username rules, and password length enforcement
- **SQL Injection Prevention**: All database queries use prepared statements
- **Session Management**: In-memory session tokens with login/logout support
- **Audit Logging**: SLF4J-based logging for login attempts and account changes

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Build and Run

```bash
mvn clean package
mvn javafx:run
```

### Configuration

Edit `src/main/resources/config.properties`:

```properties
# Switch to DATABASE for SQLite storage
storage.type=FILE

# File storage path
file.storage.path=users.json

# SQLite database path
db.path=eclipse_login.db

# BCrypt cost factor (higher = more secure but slower)
security.bcrypt.cost=12

# Minimum password length
security.password.min.length=8
```

## Project Structure

```
src/main/java/com/elililly/auth/
├── Main.java
├── config/Configuration.java
├── model/User.java
├── service/AuthenticationService.java
├── repository/
│   ├── UserRepository.java
│   ├── FileUserRepository.java
│   └── DatabaseUserRepository.java
├── database/
│   ├── DatabaseConnectionManager.java
│   └── DatabaseMigration.java
└── ui/
    ├── LoginController.java
    ├── RegistrationController.java
    └── PasswordRecoveryController.java
```

## Security Notes

- Passwords are never stored or logged in plain text
- BCrypt hashing with salt is applied at registration
- All database operations use prepared statements
- Session tokens are UUID-based and stored in memory only
Custom Eclipse login system with file/database storage support
