#!/bin/bash

# Ensure loader is available
EXE_FILE_NAME=${EXE_FILE_NAME:-$(which tsbs_load_griddb)}
if [[ -z "$EXE_FILE_NAME" ]]; then
    echo "tsbs_load_timescaledb not available. It is not specified explicitly and not found in \$PATH"
    exit 1
fi

# Load parameters - common
DATA_FILE_NAME=${DATA_FILE_NAME:-griddb-data.gz}
EXE_DIR=${EXE_DIR:-$(dirname $0)}
source ${EXE_DIR}/load_common.sh


cat ${DATA_FILE} | gunzip | $EXE_FILE_NAME  --workers=1

