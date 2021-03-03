# anomaly-detection-with-sklearn
Project code for GridDB anomaly detection article

## Requirements
- Python
- Docker and docker-compose

## Environment Variables
Create a `.env` file in the root with the following variables:

- `NUM_OBSERVATIONS`: (integer) the number of artificial data points to generate; default is 500
- `FREQ`: (string) a valid pandas `freq` (frequency) string; default is "15T" (every 15 minutes)
- `OUTLIER_FRACTION`: (float) percentage of total observations considered to be outliers; value must be between 0 to 1; default is 0.02 (2 percent)

## Run
1. Run GridDB server, Jupyter server, and initialization process:
    ```console
    $ docker-compose up --build
    ```
2. Initialize HTTP request dataset:
    ```console
    # Docker exec into jupyter container
    $ docker exec -it NAME_OF_JUPYTER_CONTAINER /bin/bash

    # Once inside container, run HTTP dataset initializer script
    $ python utils/init_http.py
    ```
3. Find URL for Jupyter server in logs and paste it onto a browser, e.g:
    ```
    http://127.0.0.1:8888/?token=39006x20aamc59c2f209e82ee61fdaf9c81501daa3901e0w
    ```

Stop the containers with:

```console
$ docker-compose down
```
