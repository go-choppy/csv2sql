setlocal
set JAVA_CMD=java

set DATA_DIR=<C:\\>

:: Mode
set STRICT_MODE=true

:: POSTGRESQL
:: set DATABASE_TYPE=postgresql
:: set POSTGRES_HOST=localhost
:: set POSTGRES_PORT=5432
:: set POSTGRES_DB=csv2sql
:: set POSTGRES_USER=postgres
:: set POSTGRES_PASS=password

:: MYSQL
set DATABASE_TYPE=mysql
set MYSQL_HOST=<localhost>
set MYSQL_PORT=3306
set MYSQL_DB=<csv2sql>
set MYSQL_USER=<MYSQL>
set MYSQL_PASS=<password>

:: Notification
set NOTIFICATION_ENABLED=true
set NOTIFICATION_PLUGIN=metabase
set NOTIFICATION_URL=<metabase-url>
set DATASET_ID=<database-id>
set NOTIFICATION_TYPES=rescan_values,sync_schema
set AUTH_TYPE=cookie
set AUTH_KEY=<username>
set AUTH_VALUE=<password>

:: Dingtalk
set ACCESS_TOKEN=<access-token>
set SECRET=<secret>
set USERNAME=%username%

%JAVA_CMD% -jar csv2sql-0.1.2-SNAPSHOT-standalone.jar
pause