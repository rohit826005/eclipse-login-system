# SETUP.md - Developer Setup Guide

## Prerequisites

| Tool    | Version  |
|---------|----------|
| Java    | 17+      |
| Maven   | 3.8+     |

## Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/rohit826005/eclipse-login-system.git
   cd eclipse-login-system
   ```

2. **Build the project**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

## Storage Modes

### File Mode (default)
No extra setup needed. Users are stored in `users.json` in the working directory.

### Database Mode
1. Open `src/main/resources/config.properties`
2. Change `storage.type=FILE` to `storage.type=DATABASE`
3. Optionally change `db.path` to a custom SQLite file path
4. Restart the application — the schema is auto-created on first run

## Running Tests

```bash
mvn test
```

## IDE Setup

### IntelliJ IDEA
1. File → Open → select the project directory
2. Maven will auto-import dependencies
3. Run `Main.java` directly

### Eclipse
1. File → Import → Existing Maven Projects
2. Select the project directory
3. Run `Main.java` as a Java application
