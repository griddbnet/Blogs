If you are thinking about switching to the [GridDB Cloud Azure Marketplace instance](https://azuremarketplace.microsoft.com/en-us/marketplace/apps/2812187.griddb_cloud_payasyougo?tab=overview), first, you can read about how to do that here: [GridDB Cloud on Microsoft Azure Marketplace](https://griddb.net/en/blog/griddb-cloud-azure-marketplace/). Second, you may be worried about how you may transfer your existing data from your [GridDB Free Plan](https://form.ict-toshiba.jp/download_form_griddb_cloud_freeplan_e?utm_source=griddbnet&utm_medium=blog-migration), from your local GridDB CE instance, or even from Postgresql.

In this blog, we will walkthrough the migration process of moving your data from a GridDB Free Plan, a local GridDB CE instance, and another third party database (postgresql in this case). The process is different for each one, so let's go through them 1-by-1.

## Migrating from GridDB Free Plan

First of all, if you are unsure what the GridDB Free Plan is, you can look here: [GridDB Cloud Quick Start Guide](https://griddb.net/en/blog/griddb-cloud-quick-start-guide/)

This is by far the easiest method of conducting a full-scale migration. The high level overview is that the TDSL (Toshiba Digital Solution) support team will handle everything for you. 

### TDSL Support 

When you sign up for the GridDB Pay As You Go plan, as part of the onboarding process, you will receive an email with the template you will need to use when contacting support for various functions, including data migration! So, grab your pertinent information (contract ID, GridDB ID, etc) and the template and let's send an email.

Compose an email to `tdsl-ms-support@toshiba-sol.co.jp` with the following template

```bash
Contract ID: [your id]
GridDB ID: [your id]
Name: Israel Imru
E-mail: imru@fixstars.com
Inquiry Details: I would like to migrate from my GridDB Free Plan Instance to my GridDB pay as you go plan
Occurrence Date:  --
Collected Information:  --
```

The team will usually respond within one business day to confirm your operation and with further instructions. For me, they sent me the following:

```bash
Please perform the following operations in the management GUI of the source system.
After completing the operations, inform us of the date and time when the operations were performed.

1. Log in to the management GUI.

2. From the menu on the left side of the screen, click [Query].

3. Enter the following query in the [QUERY EDITOR]: SELECT 2012

4. Click the [Execute] button

Best regards,

Toshiba Managed Services Support Desk
```

Once I ran the query they asked me, I clicked on query history, and copied the timestamp and sent that over to them. That was all they needed -- armed with this information, they told me to wait 1-2 business days and they would seamlessly migrate my instance along with an estimated time slot when the migration would be completed. 

Once it was done, all of my data, including the IP Whitelist and my Portal Users were all copied over to my pay as you go plan. Cool!


## Migrating from Postgresql

There is no official way of doing conducting this sort of migration, so for now, we can try simply exporting our tables into CSV files and then importing those files individually into our Cloud instance. Luckily with the [GridDB Cloud CLI tool](https://griddb.net/en/blog/griddb-cloud-cli/) this process is much easier than ever before.

So let's first export our data and go from there.

### Exporting Postgresql Data

First, the dataset I'm working with here is simply dummy data I ingested using a python script. Here's the script: 

```python
import psycopg2
import psycopg2.extras
from faker import Faker
import random
import time

# --- YOUR DATABASE CONNECTION DETAILS ---
# Replace with your actual database credentials
DB_NAME = "template1"
DB_USER = "postgres"
DB_PASSWORD = "xe$$j4o8"
DB_HOST = "localhost"  # Or your DB host
DB_PORT = "5432"       # Default PostgreSQL port

# --- DATA GENERATION SETTINGS ---
NUM_RECORDS = 50000

# Initialize Faker
fake = Faker()

# Generate a list of fake records
print(f"Generating {NUM_RECORDS} fake records...")
records_to_insert = []
for _ in range(NUM_RECORDS):
    name = fake.catch_phrase()  # Using a more specific Faker provider
    quantity = random.randint(1, 1000)
    price = round(random.uniform(0.50, 500.00), 2)
    records_to_insert.append((name, quantity, price))
print("Finished generating records.")


# SQL statements
create_table_query = """
CREATE TABLE IF NOT EXISTS sample_data (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER,
    price REAL
);
"""

# Using execute_batch is much more efficient for large inserts
insert_query = "INSERT INTO sample_data (name, quantity, price) VALUES %s;"

conn = None
try:
    # Establish a connection to the database
    conn = psycopg2.connect(
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
        host=DB_HOST,
        port=DB_PORT
    )

    # Create a cursor
    cur = conn.cursor()

    # Create the table if it doesn't exist
    print("Ensuring 'sample_data' table exists...")
    cur.execute(create_table_query)

    # Optional: Clean the table before inserting new data
    print("Clearing existing data from the table...")
    cur.execute("TRUNCATE TABLE sample_data RESTART IDENTITY;")

    # Start the timer
    start_time = time.time()

    print(f"Executing bulk insert of {len(records_to_insert)} records...")
    psycopg2.extras.execute_values(
        cur,
        insert_query,
        records_to_insert,
        template=None,
        page_size=1000  # The number of rows to send in each batch
    )
    print("Bulk insert complete.")

    # Commit the changes to the database
    conn.commit()

    # Stop the timer
    end_time = time.time()
    duration = end_time - start_time

    print(f"Successfully inserted {cur.rowcount} rows in {duration:.2f} seconds.")

    # Close the cursor
    cur.close()

except (Exception, psycopg2.DatabaseError) as error:
    print(f"Error while connecting to or working with PostgreSQL: {error}")
    if conn:
        conn.rollback()  # Roll back the transaction on error

finally:
    # Close the connection if it was established
    if conn is not None:
        conn.close()
        print("Database connection closed.")
```

Once you run this script, you will have 50k rows in your PSQL instance. Now let's export this to CSV:

```bash
$ psql --host 127.0.0.1 --username postgres --password --dbname template1

psql (14.18 (Ubuntu 14.18-0ubuntu0.22.04.1))
SSL connection (protocol: TLSv1.3, cipher: TLS_AES_256_GCM_SHA384, bits: 256, compression: off)
Type "help" for help.

template1=# select COUNT(*) from sample_data;
 count 
-------
 50000
(1 row)

template1=# COPY sample_data TO '/tmp/sample.csv' WITH (FORMAT CSV, HEADER);
COPY 50000
template1=# \q
```

And now that we have our CSV data, let's install the CLI Tool and ingest it.

### Ingesting CSV Data into GridDB Cloud

You can download the latest CLI Tool from the Github releases page:  [https://github.com/Imisrael/griddb-cloud-cli/releases](https://github.com/Imisrael/griddb-cloud-cli/releases). For me, I installed the `.deb` file

```bash
$ wget https://github.com/Imisrael/griddb-cloud-cli/releases/download/v0.1.4/griddb-cloud-cli_0.1.4_linux_amd64.deb
$ sudo dpkg -i griddb-cloud-cli_0.1.4_linux_amd64.deb
$ vim ~/.griddb.yaml
```

And enter your credentials: 

```bash
cloud_url: "https://cloud97.griddb.com:443/griddb/v2/gs_clustermfclo7/dbs/ZQ8"
cloud_username: "kG-israel"
cloud_pass: "password"
```

And ingest:

```bash
$ griddb-cloud-cli ingest /tmp/sample.csv

✔ Does this container already exist? … NO
Use CSV Header names as your GridDB Container Col names? 
id,name,quantity,price
✔ Y/n … YES
✔ Container Name: … migrated_data
✔ Choose: … COLLECTION
✔ Row Key? … true
✔ (id) Column Type … INTEGER
✔ Column Index Type1 … TREE
✔ (name) Column Type … STRING
✔ (quantity) Column Type … INTEGER
✔ (price) Column Type … FLOAT
✔ Make Container? 
{
    "container_name": "migrated_data",
    "container_type": "COLLECTION",
    "rowkey": true,
    "columns": [
        {
            "name": "id",
            "type": "INTEGER",
            "index": [
                "TREE"
            ]
        },
        {
            "name": "name",
            "type": "STRING",
            "index": null
        },
        {
            "name": "quantity",
            "type": "INTEGER",
            "index": null
        },
        {
            "name": "price",
            "type": "FLOAT",
            "index": null
        }
    ]
} … YES
{"container_name":"migrated_data","container_type":"COLLECTION","rowkey":true,"columns":[{"name":"id","type":"INTEGER","index":["TREE"]},{"name":"name","type":"STRING","index":null},{"name":"quantity","type":"INTEGER","index":null},{"name":"price","type":"FLOAT","index":null}]}
201 Created
Container Created. Starting Ingest
0 id id
1 name name
2 quantity quantity
3 price price
✔ Is the above mapping correct? … YES
Ingesting. Please wait...
Inserting 1000 rows
200 OK
Inserting 1000 rows
200 OK
```

And after some time, your data should be ready in your GridDB Cloud instance! 

```bash
$ griddb-cloud-cli sql query -s "SELECT COUNT(*) from migrated_data"

[{"stmt": "SELECT COUNT(*) from migrated_data" }]
[[{"Name":"","Type":"LONG","Value":50000}]]
```

And another confirmation

```bash
$ griddb-cloud-cli read migrated_data -p -l 1
[ { "name": "migrated_data", "stmt": "select * limit 1", "columns": null, "hasPartialExecution": true }]
[
  [
    {
      "Name": "id",
      "Type": "INTEGER",
      "Value": 1
    },
    {
      "Name": "name",
      "Type": "STRING",
      "Value": "Enterprise-wide multi-state installation"
    },
    {
      "Name": "quantity",
      "Type": "INTEGER",
      "Value": 479
    },
    {
      "Name": "price",
      "Type": "FLOAT",
      "Value": 194.8
    }
  ]
]
```

## Migrating from GridDB CE

