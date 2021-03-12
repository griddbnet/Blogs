#!/bin/bash
sleep 5

python init_griddb.py

exec uvicorn app.main:app --host 0.0.0.0 --port 80