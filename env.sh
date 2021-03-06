#!/bin/bash

export DATA_DIR=<data_dir>

# Mode
export STRICT_MODE=true

# POSTGRESQL
# export DATABASE_TYPE=postgresql
# export POSTGRES_HOST=localhost
# export POSTGRES_PORT=5432
# export POSTGRES_DB=csv2sql
# export POSTGRES_USER=postgres
# export POSTGRES_PASS=password

# MYSQL
export DATABASE_TYPE=mysql
export MYSQL_HOST=<localhost>
export MYSQL_PORT=3306
export MYSQL_DB=<csv2sql>
export MYSQL_USER=<MYSQL>
export MYSQL_PASS=<password>

# Notification
export NOTIFICATION_ENABLED=true
export NOTIFICATION_PLUGIN=metabase
export NOTIFICATION_URL=<base_url>
export DATASET_ID=<database_id>
export NOTIFICATION_TYPES=rescan_values,sync_schema
export AUTH_TYPE=cookie
export AUTH_KEY=<username>
export AUTH_VALUE=<password>

# Dingtalk
export ACCESS_TOKEN=<access-token>
export SECRET=<secret>
export USERNAME=`whoami`

java -jar csv2sql-0.1.2-SNAPSHOT-standalone.jar
