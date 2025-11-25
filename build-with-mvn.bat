@echo off
rem Build with local Maven (Windows)
mvn clean package
echo Built jar in target\ directory. Run: java -jar target\atm-management-1.0-SNAPSHOT-shaded.jar
