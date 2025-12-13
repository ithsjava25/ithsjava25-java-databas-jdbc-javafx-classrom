[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/339Lr3BJ)
### How the tests work (and Docker requirement)

This project ships with an end‑to‑end CLI integration test suite that uses Testcontainers to spin up a temporary MySQL database. 
Because Testcontainers needs a container runtime, you must have Docker running on your machine to execute the tests.

- What the tests do
  - Start a throwaway MySQL container using the configuration in `src/main/resources/myconfig` and seed data from `src/main/resources/init.sql`.
  - Set the following Java system properties so the application can connect to that database:
    - `APP_JDBC_URL`
    - `APP_DB_USER`
    - `APP_DB_PASS`
  - Drive your CLI via STDIN/STDOUT: first a login flow (username → password), then menu operations (list missions, get mission by id, count missions by year, create/update/delete account), and finally exit.

- How to run the tests
  - Ensure Docker Desktop (Windows/macOS) or Docker Engine (Linux) is running.
  - Run: `./mvnw verify`

If Docker is not running, Testcontainers will fail to start the database and tests will not run.

---

### Dev mode (optional local database during development)

When you run the application directly (without the test suite), you can let the app start a development MySQL instance for you.
Enable “dev mode” in any one of three ways:

1) Java system property (VM option)
- Add: `-DdevMode=true`

2) Environment variable
- Set: `DEV_MODE=true`

3) Program argument
- Pass: `--dev`

In dev mode, `DevDatabaseInitializer` uses Testcontainers to start MySQL and automatically sets the standard properties `APP_JDBC_URL`, `APP_DB_USER`, and `APP_DB_PASS` so the app can connect.

Example (Windows PowerShell):
```
java -DdevMode=true -jar target/app.jar
```

---

### Supplying your own database settings in IntelliJ (when not using dev mode)

If you prefer to connect to an existing database (local or remote) and you are not using dev mode, configure the Run/Debug Configuration in IntelliJ IDEA. The application reads settings with clear precedence: Java system properties (VM options) first, then environment variables.

Required keys:
- `APP_JDBC_URL`  (e.g., `jdbc:mysql://localhost:3306/testdb`)
- `APP_DB_USER`
- `APP_DB_PASS`

Steps (IntelliJ IDEA):
1) Open Run → Edit Configurations…
2) Create or select an Application configuration for `com.example.Main`.
3) Choose one of the following ways to provide settings:
   - VM options (recommended; highest precedence)
     - Put this in the field “VM options”:
       ```
       -DAPP_JDBC_URL=jdbc:mysql://localhost:3306/testdb -DAPP_DB_USER=user -DAPP_DB_PASS=pass
       ```
   - Environment variables
     - Click “Modify options” → check “Environment variables” (if not visible), then click the `…` button and add:
       - `APP_JDBC_URL = jdbc:mysql://localhost:3306/testdb`
       - `APP_DB_USER = user`
       - `APP_DB_PASS = pass`
4) Apply and Run.

Notes
- You can mix both, but values in VM options override environment variables.
- If any of the three are missing or blank, the app fails fast with a clear error.

Running in dev mode from IntelliJ
- In the same Run/Debug Configuration, you can enable dev mode in any one of these ways:
  - VM option: add `-DdevMode=true`
  - Environment variable: add `DEV_MODE=true`
  - Program arguments: add `--dev` to the “Program arguments” field

Tip: Maven test runs inside IntelliJ
- If you run the Maven goal `verify` or the integration tests from IntelliJ, ensure Docker is running. Testcontainers will manage the database and set `APP_JDBC_URL`, `APP_DB_USER`, and `APP_DB_PASS` automatically for the test JVM.

---

### Assignment requirements

G (base level)
- Implement the CLI application logic in `Main` starting at the `run()` method so that the provided tests pass. Concretely, your CLI should:
  - Prompt for `Username:` and then `Password:` on startup and validate them against the `account` table (`name` + `password`).
  - If the login is invalid, print a message containing the word `invalid` and allow exiting via option `0`.
  - If the login is valid, present a menu with options:
   ```
      1) List moon missions (prints spacecraft names from `moon_mission`).
      2) Get a moon mission by mission_id (prints details for that mission).
      3) Count missions for a given year (prompts: year; prints the number of missions launched that year).
      4) Create an account (prompts: first name, last name, ssn, password; prints confirmation).
      5) Update an account password (prompts: user_id, new password; prints confirmation).
      6) Delete an account (prompts: user_id; prints confirmation).
      0) Exit.
   ```
  - Use the DB settings provided via `APP_JDBC_URL`, `APP_DB_USER`, `APP_DB_PASS` (already resolved in `Main`).

Notes
- Seed data in `init.sql` includes a known account used by the tests (e.g., username `AngFra`, password `MB=V4cbAqPz4vqmQ`).
- The tests are ordered to run login checks first and then the other menu actions.

---

VG (extra credit)
- Implement a Repository pattern so that all database access lives inside repository classes, and the rest of your application depends only on repository interfaces. Recommended approach:
  - Create a `DataSource` once at startup (using the connection settings above) and inject it into your repositories by constructor injection. For a minimal setup, you can implement a small `SimpleDriverManagerDataSource` that delegates to `DriverManager.getConnection(...)`. This keeps repositories independent of configuration and lets you upgrade to a connection pool (e.g., HikariCP) later without changing repository code.
  - Define `AccountRepository` and `MoonMissionRepository` and provide JDBC implementations.
  - In `Main`, resolve configuration, construct the `DataSource`, instantiate repositories.
