"""
Module that contains helpers for generating toy data and 
loading into GridDB
"""
from jaydebeapi import Connection
import numpy as np
import pandas as pd


rng = np.random.RandomState(42)


def load_into_sensor_table(conn: Connection, data: pd.DataFrame) -> int:
    """Inserts data into sensor table."""
    cursor = conn.cursor()
    row_count = 0

    try:
        # Create table
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS power_sensor(
                ts INTEGER PRIMARY KEY,
                kwh FLOAT
            )
            """
        )

        # Insert all rows
        for row in data.itertuples():
            cursor.execute(
                """
                INSERT INTO power_sensor(ts, kwh)
                VALUES (?, ?)
                """,
                (row.ts, row.kwh)
            )
        conn.commit()

        # Check if successfully inserted
        cursor.execute("SELECT * FROM power_sensor")
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


def create_sensor_data(
    size: int,
    freq: str,
    outlier_fraction: float
) -> pd.DataFrame:
    """Generates toy time-series dataset."""
    # Generate normally-distributed values
    ts_index = pd.date_range("2020-01-15", periods=size, freq=freq)
    sensor_readings = rng.normal(loc=4, scale=0.6, size=size)
    df = pd.DataFrame(sensor_readings, index=ts_index, columns=["kwh"])

    # Replace some values with outliers at random timestamps
    num_outliers = int(outlier_fraction * size)
    outlier_ts = np.random.choice(df.index, size=num_outliers, replace=False)
    df.loc[outlier_ts, "kwh"] = rng.normal(
        loc=9,
        scale=0.01,
        size=num_outliers
    )

    # Add integer timestamp column
    df['ts'] = df.apply(lambda row: int(row.name.timestamp()), axis=1)

    return df


