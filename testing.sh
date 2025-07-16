#!/bin/bash

DOWNLOADS_DIR="/Users/israelimru/Downloads"

FILE_COUNT=$(ls -1A "$DOWNLOADS_DIR" | wc -l | xargs)

TOTAL_SIZE=$(du -sh "$DOWNLOADS_DIR" | awk '{print $1}')

LOG_DATA="NOW(),$FILE_COUNT,$TOTAL_SIZE"

griddb-cloud-cli create /Users/israelimru/download_table.json -f
griddb-cloud-cli put -n download_data -v $LOG_DATA

echo "Log complete."