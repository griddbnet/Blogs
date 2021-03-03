#!/bin/bash

echo Starting initializer script
sleep 3

echo Running database initialization process

python utils/init_sensor_data.py
