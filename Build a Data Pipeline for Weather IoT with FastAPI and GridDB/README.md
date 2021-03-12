# fastapi-griddb-data-pipeline
Project code for the article "Build a data pipeline for weather IoT with FastAPI and GridDB".

## Requirements
- Python 3.8+
- FastAPI
- GridDB
- Docker and docker-compose

## Dockerfiles
- [Dockerfile for GridDB](https://github.com/griddb/griddb-docker)

## Running

```console
docker-compose up --build
```


## Usage
Save new record:

```console
$ curl -X POST http://localhost/record
```

Retrieve records from the last 10 minutes:
```console
$ curl http://localhost/retrieve?minutes=10
```

**Note:** If no query parameter for `minutes` is passed, the default is 5.
