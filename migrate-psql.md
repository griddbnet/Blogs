In a previous article, we talked about some new features in the [GridDB Cloud CLI Tool](https://griddb.net/en/blog/griddb-cloud-cli/) which showcased the ability to make tables based on schemas in the form of JSON files. Included in that was also the ability to migrate from GridDB Community Edition (on prem) to the Cloud in a just a few commands; you can read that article here: [New Features of the GridDB Cloud CLI Tool](https://griddb.net/en/blog/new-features-griddb-cloud-cli/). In this article, we will again feature the GridDB Cloud CLI Tool, but this time we want to showcase the ability to migrate from your on prem PostgreSQL database directly to your GridDB Cloud instance.

We will get into the details later in the article, but here is a basic rundown of the steps required to make this work: 

1. Export PostgreSQL tables as CSV row data
2. Export schemas of all PostgreSQL tables as JSON file
3. Use GridDB Cloud CLI Tool to transform the data, create the corresponding tables, and then load the data into GridDB Cloud

## Scripts to Export PostgreSQL

Before we showcase running an example of this process, let's first take a look at the scripts needed to export the data from your PostgreSQL instance into a more neutral and workable data format (CSV). We will use two different scripts in this phase, one for extract row data, and the other to grab 'meta'/schema data; both of these scripts rely on the `psql` command to directly communicate with the database to grab the data we need.

### Script for Row Data

Here is the script to extract your row data; it will generate a unique CSV file for each table in your database, with the filename corresponding directly to the name of the table -- handy!

```bash
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
```

The script itself is self-explanatory: it exports all tables into the current working directory with the header as the top row and every subsequent row being the data from the table.

### Script for Schema Data

This script is a bit more clever and more of a unique feature for PostgreSQL. It utilizes its `json_agg()` function which "is an aggregate function that collects values from multiple rows and returns them as a single JSON array." [source](https://neon.com/docs/functions/json_agg). Here is what the script looks like: 

```bash
#!/bin/bash

psql -d postgres -X -A -t -c "
WITH column_details AS (
    -- First, get all the column info for each table
    SELECT 
        table_name, 
        json_agg(
            json_build_object(
                'column_name', column_name, 
                'data_type', udt_name, 
                'is_nullable', is_nullable,
                'column_default', column_default
            ) ORDER BY ordinal_position
        ) AS columns 
    FROM 
        information_schema.columns 
    WHERE 
        table_schema = 'public' 
    GROUP BY 
        table_name
),
primary_key_details AS (
    -- Next, find the primary key columns for each table
    SELECT 
        kcu.table_name, 
        json_agg(kcu.column_name) AS pk_columns
    FROM 
        information_schema.table_constraints AS tc 
    JOIN 
        information_schema.key_column_usage AS kcu 
        ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
    WHERE 
        tc.constraint_type = 'PRIMARY KEY' 
        AND tc.table_schema = 'public'
    GROUP BY 
        kcu.table_name
)
-- Finally, join them together
SELECT 
    json_object_agg(
        cd.table_name, 
        json_build_object(
            'primary_key', COALESCE(pkd.pk_columns, '[]'::json),
            'columns', cd.columns
        )
    )
FROM 
    column_details cd
LEFT JOIN 
    primary_key_details pkd ON cd.table_name = pkd.table_name;
" > schema.json
```

If you were just grabbing the schema, I think this script would be about `half` of this length, but we actually are grabbing the primary key as well which requires a `JOIN` operation. We grab this data in order to ensure we migrate time series tables into `TIME_SERIES` containers on the GridDB side (more on that later).

## Changes to the GridDB Cloud CLI Tool

To get this process to work, we needed to first separate out the migrate tool. Prior to this release, the migrate tool only expected to work with GridDB CE, and so, the command worked like this: `griddb-cloud-cli migrate [griddb-out-directory]`. Now obviously this no longer works as we need to indicate to the tool what sort of database we are importing to GridDB Cloud.

So what we did was separate out the commands as sub commands, and this meant keeping the more generic functions that work for both in the root of the command -- things like reading a CSV file, or parsing a JSON file -- and keeping the DB-specific functions inside of the respective subcommand files. So now our `migrate/` directory has three files instead of one: `migrateCmd.go, migrateGridDB.go, and migratePSQL.go`

Here's a brief example of a function which is unique to the PostgreSQL import process

```go
func typeSwitcher(s string) string {
	switch s {
	case "bool":
		return "BOOL"
	case "char", "varchar", "text":
		return "STRING"
	case "int", "int2", "int4":
		return "INTEGER"
	case "int8":
		return "LONG"
	case "decimal", "real", "numeric":
		return "FLOAT"
	case "float", "float8":
		return "DOUBLE"
	case "timetz", "timestamptz":
		return "TIMESTAMP"
	default:
		return strings.ToUpper(s)

	}
}
```

## Migrating from PostgreSQL

Now that we have our data ready for use, let's run the migration tool and see the results. First, place the contents of your exporting efforts into their own directory

```bash
$ mkdir psql_exported
$ mv *.csv psql_exported/
$ mv schema.json psql_exprted/
```

And now we can run the tool: 

```bash
$ griddb-cloud-cli migrate psql psql_exported/
```

    {"container_name":"customers","container_type":"COLLECTION","rowkey":false,"columns":[{"name":"customer_id","type":"INTEGER","index":null},{"name":"first_name","type":"STRING","index":null},{"name":"last_name","type":"STRING","index":null},{"name":"email","type":"STRING","index":null},{"name":"phone_number","type":"STRING","index":null},{"name":"address","type":"STRING","index":null},{"name":"created_at","type":"TIMESTAMP","index":null}]}
    ✔ Make Container? 
    {
        "container_name": "customers",
        "container_type": "COLLECTION",
        "rowkey": false,
        "columns": [
            {
                "name": "customer_id",
                "type": "INTEGER",
                "index": null
            },
            {
                "name": "first_name",
                "type": "STRING",
                "index": null
            },
            {
                "name": "last_name",
                "type": "STRING",
                "index": null
            },
            {
                "name": "email",
                "type": "STRING",
                "index": null
            },
            {
                "name": "phone_number",
                "type": "STRING",
                "index": null
            },
            {
                "name": "address",
                "type": "STRING",
                "index": null
            },
            {
                "name": "created_at",
                "type": "TIMESTAMP",
                "index": null
            }
        ]
    } … YES
    201 Created
    inserting into (customers). csv: psql_exported/customers.csv
    200 OK
    {"container_name":"products","container_type":"COLLECTION","rowkey":false,"columns":[{"name":"id","type":"INTEGER","index":null},{"name":"name","type":"STRING","index":null},{"name":"category","type":"STRING","index":null},{"name":"price","type":"FLOAT","index":null},{"name":"stock_quantity","type":"INTEGER","index":null}]}
    ✔ Make Container? 
    {
        "container_name": "products",
        "container_type": "COLLECTION",
        "rowkey": false,
        "columns": [
            {
                "name": "id",
                "type": "INTEGER",
                "index": null
            },
            {
                "name": "name",
                "type": "STRING",
                "index": null
            },
            {
                "name": "category",
                "type": "STRING",
                "index": null
            },
            {
                "name": "price",
                "type": "FLOAT",
                "index": null
            },
            {
                "name": "stock_quantity",
                "type": "INTEGER",
                "index": null
            }
        ]
    } … YES
    201 Created
    inserting into (products). csv: psql_exported/products.csv
    200 OK

And now, of course, is the last step: checking to make sure our data was successfully migrated.

```bash
$ griddb-cloud-cli show customers                              
{
    "container_name": "customers",
    "container_type": "COLLECTION",
    "rowkey": false,
    "columns": [
        {
            "name": "customer_id",
            "type": "INTEGER",
            "index": []
        },
        {
            "name": "first_name",
            "type": "STRING",
            "index": []
        },
        {
            "name": "last_name",
            "type": "STRING",
            "index": []
        },
        {
            "name": "email",
            "type": "STRING",
            "index": []
        },
        {
            "name": "phone_number",
            "type": "STRING",
            "index": []
        },
        {
            "name": "address",
            "type": "STRING",
            "index": []
        },
        {
            "name": "created_at",
            "type": "TIMESTAMP",
            "timePrecision": "MILLISECOND",
            "index": []
        }
    ]
}
```

And a quick read:

```bash
$ griddb-cloud-cli read customers -r
[ { "name": "customers", "stmt": "select * limit 50", "columns": null, "hasPartialExecution": true }]
customer_id,first_name,last_name,email,phone_number,address,created_at,
[1 Alice Johnson alice.j@email.com 555-0101 123 Maple St, Springfield 2025-09-04T06:32:05.700Z]
[2 Bob Smith bob.smith@email.com 555-0102 456 Oak Ave, Shelbyville 2025-09-04T06:32:05.700Z]
[3 Charlie Brown charlie@email.com 555-0103 789 Pine Ln, Capital City 2025-09-04T06:32:05.700Z]
[4 Diana Prince diana.p@email.com  901 Birch Rd, Themyscira 2025-09-04T06:32:05.700Z]
[5 Ethan Hunt ethan.hunt@email.com 555-0105 1122 Mission St, Los Angeles 2025-09-04T06:32:05.700Z]
```

And for fun: 

```bash
griddb-cloud-cli read graph products --columns 'stock_quantity'
["stock_quantity"]
[ { "name": "products", "stmt": "select * limit 50", "columns": ["stock_quantity"], "hasPartialExecution": true }]
 936 ┤                                               ╭╮
 906 ┤                                               ││
 876 ┤                                               ││
 845 ┤                                               │╰╮       ╭╮
 815 ┤                                               │ │       ││
 785 ┤                                               │ │       ││
 755 ┤                                               │ │  ╭─╮  ││
 724 ┤                                               │ │  │ │ ╭╯│
 694 ┤                                               │ │  │ │ │ ╰╮               ╭╮
 664 ┤                                              ╭╯ │  │ ╰╮│  │               ││
 633 ┤                                              │  │  │  ││  │               ││
 603 ┤                                              │  ╰╮╭╯  ╰╯  │               ││
 573 ┤                                              │   ││       │              ╭╯│
 543 ┤                                              │   ││       │              │ │
 512 ┤                                              │   ││       │              │ ╰╮            ╭╮
 482 ┤                                              │   ╰╯       │        ╭╮    │  │            │╰╮
 452 ┤                                              │            │        ││   ╭╯  │            │ │
 421 ┤      ╭─╮                                     │            │        │╰╮  │   │            │ │
 391 ┤      │ │        ╭╮                          ╭╯            │       ╭╯ │ ╭╯   │            │ │   ╭╮
 361 ┤      │ │        ││                          │             ╰╮      │  ╰─╯    │            │ │   │╰╮
 331 ┤      │ │        │╰╮                         │              │      │         │            │ │  ╭╯ ╰
 300 ┤     ╭╯ │       ╭╯ │       ╭╮                │              │      │         ╰╮    ╭╮    ╭╯ │  │
 270 ┤     │  │       │  ╰╮      ││                │              │      │          │   ╭╯╰╮   │  ╰╮ │
 240 ┤     │  ╰╮      │   │     ╭╯│               ╭╯              │     ╭╯          │  ╭╯  ╰╮  │   │ │
 209 ┤    ╭╯   │      │   ╰╮    │ ╰╮         ╭─╮  │               │     │           │  │    ╰╮ │   │╭╯
 179 ┤   ╭╯    │     ╭╯    │   ╭╯  │      ╭──╯ │ ╭╯               │     │           │╭─╯     │ │   ││
 149 ┤  ╭╯     │     │     ╰╮  │   ╰╮    ╭╯    ╰╮│                │     │           ╰╯       ╰╮│   ││
 119 ┤ ╭╯      │  ╭╮ │      │╭─╯    │   ╭╯      ╰╯                │     │                     ╰╯   ││
  88 ┤╭╯       │ ╭╯╰─╯      ╰╯      ╰─╮╭╯                         │     │                          ╰╯
  58 ┼╯        ╰─╯                    ╰╯                          │   ╭─╯
  28 ┤                                                            ╰───╯
                                       Col names from container products

                                                ■ stock_quantity
```

## Conclusion

Never has it been easier to migrate from an on-prem SQL database to GridDB Cloud, and we hope these tools make any of those looking to try out GridDB Cloud a much simpler process.
