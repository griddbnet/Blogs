#!/bin/bash

# Check if a database name was provided.
if [ -z "$1" ]; then
  echo "Usage: $0 <database_name>"
  exit 1
fi

DB_NAME=$1
EXPORT_DIR="." # Export to the current directory.

# Get a list of all tables in the 'public' schema.
TABLES=$(psql -d $DB_NAME -t -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE';")

# Loop through the tables and export each one to a CSV file.
for TBL in $TABLES; do
  echo "Exporting table: $TBL"
  psql -d $DB_NAME -c "\copy (SELECT * FROM $TBL) TO '$EXPORT_DIR/$TBL.csv' WITH (FORMAT CSV, HEADER);"
done

echo "Export complete."
