"""
Script to initialize http data table
"""
import os

from jaydebeapi import Connection
import pandas as pd

from helpers.db import get_connection


def load_into_http_table(conn: Connection, data: pd.DataFrame) -> int:
    """Inserts data into HTTP traffic table."""
    cursor = conn.cursor()
    row_count = 0

    try:
        # Create table
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS http_traffic(
                duration FLOAT,
                src_bytes FLOAT,
                dst_bytes FLOAT,
                is_attack INTEGER
            )
            """
        )

        # Insert all rows
        for row in data.itertuples():
            cursor.execute(
                """
                INSERT INTO http_traffic (duration, src_bytes, dst_bytes, is_attack)
                VALUES (?, ?, ?, ?)
                """,
                (
                    row.duration,
                    row.src_bytes,
                    row.dst_bytes,
                    row.is_attack,
                )
            )
        conn.commit()

        # Check if successfully inserted
        cursor.execute("SELECT * FROM http_traffic")
        result = cursor.fetchall()
        if len(result) == 0:
            raise ValueError('No rows inserted')

        row_count = len(result)
    except Exception as e:
        conn.rollback()
        print(f'Error loading data into db: {e}')
    finally:
        cursor.close()

    return row_count


if __name__ == '__main__':
    print('Initializing HTTP traffic table')
    conn = get_connection()

    print('Reading CSV data')
    dirpath = os.path.abspath(os.path.dirname(__file__))
    filepath = os.path.join(dirpath, 'datasets/http.csv')
    df = pd.read_csv(filepath)

    print(f'Detected a total of {len(df)} data points ')

    print('Storing data into table')
    row_count = load_into_http_table(conn, df)

    print(f'Inserted a total of {row_count} rows')

    conn.close()
    print('Done')
