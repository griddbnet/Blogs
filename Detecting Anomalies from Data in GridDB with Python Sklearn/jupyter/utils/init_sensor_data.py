"""
Script that initializes database with 
connection and toy data
"""
import os

from helpers.sensor import create_sensor_data, load_into_sensor_table
from helpers.db import get_connection


NUM_OBSERVATIONS = int(os.environ.get('NUM_OBSERVATIONS', 500))
FREQ = os.environ.get('FREQ', '15T')
OUTLIER_FRACTION = float(os.environ.get('OUTLIER_FRACTION', 0.02))


if __name__ == "__main__":
    print('power sensor table')
    conn = get_connection()

    print('Generating sensor data')
    df = create_sensor_data(NUM_OBSERVATIONS, FREQ, OUTLIER_FRACTION)

    print(
        f'Generated a total of {len(df)} data points '
        f'with {OUTLIER_FRACTION * 100} percent outliers'
    )

    print('Storing data into table')
    row_count = load_into_sensor_table(conn, df)

    print(f'Inserted a total of {row_count} rows')

    conn.close()
    print('Done')
