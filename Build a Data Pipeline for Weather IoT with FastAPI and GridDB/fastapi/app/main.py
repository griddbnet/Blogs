import logging
from typing import Optional

from fastapi import FastAPI, Response, status

from .config import (
    GRIDDB_CONNECTION_PARAMS,
    GRIDDB_CONTAINER_NAME,
)
from .db import GridDBConnection
from .schema import ResponseModel
from .services.weatherstation import get_sensor_data


logger = logging.getLogger("api")


app = FastAPI()


@app.on_event("startup")
async def startup_event():
    logger.info("Server startup")

    logger.info("Initializing GridDB connection")
    gdb = GridDBConnection(GRIDDB_CONNECTION_PARAMS)
    gdb.init(GRIDDB_CONTAINER_NAME)

    app.state.griddb = gdb
    logger.info("Server Startup")


@app.on_event("shutdown")
async def shutdown_event():
    app.state.griddb.cleanup()
    logger.info("Server Shutdown")


@app.post("/record", response_model=ResponseModel)
async def save_current_sensor_data(response: Response):
    """Gets data from sensors and stores it in GridDB."""
    sensor_data = get_sensor_data()
    app.state.griddb.container.put(list(sensor_data.values()))

    response.status_code = status.HTTP_201_CREATED
    return {
        "status": "Successfully stored new reading",
        "records": 1,
        "data": [sensor_data],
    }


@app.get("/retrieve", response_model=ResponseModel)
async def get_stored_readings(minutes: Optional[int] = 5):
    """Retrieves stored readings within given number of minutes ago."""
    stmt = f"""
        select * 
        where timestamp > TIMESTAMPADD(MINUTE, NOW(), -{minutes})
        order by timestamp desc
    """
    data = app.state.griddb.execute_and_fetch(stmt, as_dict=True)

    return {
        "status": f"Retrieved stored records within last {minutes} minutes",
        "records": len(data),
        "data": data,
    }
