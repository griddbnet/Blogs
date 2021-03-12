#!/usr/bin/python
"""
Stand-alone script to initialize GridDB connection 
and create weather station container
"""
import griddb_python as griddb

from app.db.connection import GridDBConnection
from app.config import GRIDDB_CONNECTION_PARAMS, GRIDDB_CONTAINER_NAME


def main() -> None:
    try:
        gdb = GridDBConnection(GRIDDB_CONNECTION_PARAMS)

        print("Initializing GridDB connection")
        gdb.init()

        print(f"Creating container {GRIDDB_CONTAINER_NAME}")
        con_info = griddb.ContainerInfo(
            name=GRIDDB_CONTAINER_NAME,
            column_info_list=[
                ["timestamp", griddb.Type.TIMESTAMP],
                ["temperature", griddb.Type.DOUBLE],
                ["pressure", griddb.Type.DOUBLE],
                ["humidity", griddb.Type.DOUBLE],
            ],
            type=griddb.ContainerType.TIME_SERIES
        )

        gdb.create_container(con_info)

        print('Done')
    except Exception as e:
        print(f"Error initializing GridDB: {e}")


if __name__ == "__main__":
    main()
