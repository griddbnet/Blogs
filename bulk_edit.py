import psycopg2
import psycopg2.extras
from faker import Faker
import random
import time

# Replace with your actual database credentials
DB_NAME = "template1"
DB_USER = "postgres"
DB_PASSWORD = "passwordd"
DB_HOST = "localhost"  
DB_PORT = "5432"       

NUM_RECORDS = 50000

fake = Faker()

print(f"Generating {NUM_RECORDS} fake records...")
records_to_insert = []
for _ in range(NUM_RECORDS):
    name = fake.catch_phrase()
    quantity = random.randint(1, 1000)
    price = round(random.uniform(0.50, 500.00), 2)
    records_to_insert.append((name, quantity, price))
print("Finished generating records.")


create_table_query = """
CREATE TABLE IF NOT EXISTS sample_data (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER,
    price REAL
);
"""

insert_query = "INSERT INTO sample_data (name, quantity, price) VALUES %s;"

conn = None
try:
    conn = psycopg2.connect(
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
        host=DB_HOST,
        port=DB_PORT
    )

    cur = conn.cursor()

    print("Ensuring 'sample_data' table exists...")
    cur.execute(create_table_query)

    print("Clearing existing data from the table...")
    cur.execute("TRUNCATE TABLE sample_data RESTART IDENTITY;")

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

    conn.commit()

    end_time = time.time()
    duration = end_time - start_time

    print(f"Successfully inserted {cur.rowcount} rows in {duration:.2f} seconds.")

    cur.close()

except (Exception, psycopg2.DatabaseError) as error:
    print(f"Error while connecting to or working with PostgreSQL: {error}")
    if conn:
        conn.rollback()  # Roll back the transaction on error

finally:
    if conn is not None:
        conn.close()
        print("Database connection closed.")
