# Submission Report - ATM Management System

This report maps the implemented features to the college marking rubrics (GUI & Web rubrics provided).

## Key Files
- `src/main/java/atm/Account.java` (abstract base class)
- `src/main/java/atm/SavingsAccount.java`, `CurrentAccount.java` (inheritance)
- `src/main/java/atm/Transactionable.java` (interface)
- `src/main/java/atm/AccountDAO.java` (JDBC DAO, transactions)
- `src/main/java/atm/InMemoryCache.java` (Collections & Generics)
- `src/main/java/atm/TransactionWorker.java` (Multithreading)
- `src/main/java/atm/ATMGui.java` (Swing UI, main entry)
- `src/main/java/atm/HashUtil.java` (PIN hashing utility)
- `sql/init.sql` (DB schema + sample data with PIN hashes)
- `pom.xml` (Maven pom configured to build shaded jar)

## Rubric Mapping (high level)
- **OOP Concepts (Inheritance, Polymorphism, Abstraction)**: Account hierarchy and overridden withdrawal logic.
- **Exception Handling**: `DatabaseException` custom exception and try/catch around DB operations.
- **Collections & Generics**: `ConcurrentHashMap<Integer, Account>` in `InMemoryCache`.
- **Database Integration (JDBC)**: `AccountDAO` uses `PreparedStatement`, transactions, and row-locking (`FOR UPDATE`).
- **Multithreading & Synchronization**: `TransactionWorker` threads, synchronized methods in `Account`, and transactional DB updates.
- **GUI**: Swing-based interface with account login (PIN), balance view, withdraw, deposit, transfer, transaction history.
- **Security**: PIN stored as SHA-256 hash (see `HashUtil.java`). For production, use salted PBKDF2/BCrypt.
- **Build & Deployment**: Maven pom and Dockerfile included for reproducible builds and packaging.

## How to build (two options)
1. **Local Maven (recommended)**:
   - Install JDK 17+ and Maven.
   - Edit `src/main/java/atm/DBConfig.java` to set DB credentials.
   - Run: `mvn clean package`
   - Run jar: `java -jar target/atm-management-1.0-SNAPSHOT-shaded.jar`

2. **Docker (no Maven install required)**:
   - Install Docker.
   - Run: `./build-with-docker.sh` (Linux/macOS) or run the equivalent docker command in Windows PowerShell.
   - After build, run: `java -jar target/atm-management-1.0-SNAPSHOT-shaded.jar`

## Notes for graders
- The solution focuses on backend correctness (transactions, concurrency) and a simple, usable GUI.
- Additional enhancements (unit tests, salted hashes, logging) can be provided on request.
