# csv2sql

A simple ETL tool to load CSVs in a directory tree into a SQL database (MySQL/PostgreSQL/SQLite). Based on 'https://github.com/ivarthorson/csv2sql'.

Scans through the subdirectories of DATA_DIR, infers the column data types,
and stores the inferred schema in DATA_DIR so that you may manually edit it
before re-running csv2sql.

Only Support two file type: JSON and CSV (Comma Seperated Value Format)

```bash
├── $DATA_DIR              # ROOT Directory
│   ├── subdir_1
│   └── subdir_2           # subdir_2 will be a table name
│       ├── core.csv       # core.csv, csvs.csv, dates.csv will be merged into one table.
│       ├── csvs.csv
│       ├── dates.csv
```


## Usage

```bash
lein uberjar  # Build the uberjar

# Edit the following path to be the root of the CSV directory tree
DATA_DIR=/path/to/some/csvs/
AUTO_DETECT=true  # return true if the AUTO_DETECT=true, otherwise return false

# Default SQLite
SQLITE_DB_PATH=sqlite-database.db

# For MySQL
DATABASE_TYPE=mysql
MYSQL_HOST=localhost
MYSQL_PORT=5432
MYSQL_DB=csv2sql
MYSQL_USER=postgres
MYSQL_PASS=mysecretpassword

# For PostgreSQL
DATABASE_TYPE=postgresql
POSTGRES_HOST=localhost
POSTGRES_PORT=3306
POSTGRES_DB=csv2sql
POSTGRES_USER=mysql
POSTGRES_PASS=mysecretpassword

java -jar target/csv2sql-0.1.0-SNAPSHOT-standalone.jar
```


## License

Copyright © 2019 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

