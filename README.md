# ATM Management (Maven)

This Maven project builds an executable (fat) JAR containing the ATM Management System and its MySQL dependency.

## How to build (on your machine)
1. Install Java JDK 17+ and Maven (ensure `mvn` is in PATH).
2. Unzip this project and open a terminal in the project root.
3. Run:
   mvn clean package
4. Maven will produce a shaded JAR in `target/` named like `atm-management-1.0-SNAPSHOT-shaded.jar`.
5. Run the JAR (you still need a MySQL server running and the DB configured):
   java -jar target/atm-management-1.0-SNAPSHOT-shaded.jar

## Database setup
- Run the SQL script in `sql/init.sql` to create the database and sample accounts.
- Edit `src/main/java/atm/DBConfig.java` to set your DB URL, username and password before packaging (or edit before running).

## Notes
- If you prefer IntelliJ, open this folder as a Maven project (it will import dependencies automatically).
- The JAR includes MySQL connector, so you don't need to place the connector separately.
