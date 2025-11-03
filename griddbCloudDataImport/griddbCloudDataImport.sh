#!/bin/bash

# ##################################################
#
# griddbCloudDataImport.sh
#
# This script uploads the data from the designated input file to the specified container.
#
# Argument:
#   1 ContainerName
#     -> Please specify the target container name for data registration enclosed in double quotes.
#   2 FilePath
#     -> Please specify the file name or file path for registration enclosed in double quotes.
#
# Execution example:
#   griddbCloudDataImport.sh "container" "file.csv"
#   griddbCloudDataImport.sh "container" "file.json"
#
# Notes:
#   - Depending on the execution environment and the amount of data, memory may become insufficient, and data registration may fail.
#     If data registration fails, consider modifying the data volume or adjusting the [Constants] configuration.
#   - Depending on the process, many temporary files might be generated.
#     If you want to prevent excessive temporary file creation, consider modifying the data volume or adjusting the [Constants] configuration.
#
# ##################################################

# Move to the directory where the script is located
cd "$(dirname "$0")"

# Constants
###################################
# Constants that need to be changed
# Change the constants below according to your environment.
readonly WEBAPI_URL="https://cloud8737.griddb.com:443/griddb/v2/gs_clustermfcloud8737/dbs/o8O3h29r"
readonly GRIDDB_USER="S016jp7ZJi-israel"
readonly GRIDDB_PASS="israel"
readonly PROXY_SERVER=""
## Number of header lines to skip in the input CSV file
readonly SKIP_HEADER_ROWS=1
## Number of rows to split per registration.
readonly SPLIT_ROWS=10000
## Temporary file output destination
readonly TEMP_FILE_PATH="/tmp"
###################################

# The constants below do not need to be changed.
# Log file name with timestamp
readonly LOG_NAME="griddbCloudDataImport_$(date '+%Y%m%d').log"

CONTAINER_NAME="container"
FILE_PATH="file.csv"

# Functions
# Function to log informational messages
function log_info() {
	echo "[$(date '+%Y/%m/%d %H:%M:%S.%3N')] [INFO] $1" >> "${LOG_NAME}"
}
# Function to log error messages
function log_error() {
	echo "[$(date '+%Y/%m/%d %H:%M:%S.%3N')] [ERROR] $1" >> "${LOG_NAME}"
}
# Function to send data to GridDB WebAPI using curl
function send_request() {
	readonly GRIDDB_WEBAPI_URL=$(echo "${WEBAPI_URL}" | sed 's:/*$::')
	if [ -z "$PROXY_SERVER" ]; then
		# no proxy
		echo "$1" | curl -s -X PUT -H "Content-Type: application/json; charset=UTF-8" -H "Authorization:Basic ${basic_auth}" "${GRIDDB_WEBAPI_URL}/containers/${CONTAINER_NAME}/rows" -d @- -w "\n%{response_code}"
	else
		# proxy
		echo "$1" | curl -s -X PUT -H "Content-Type: application/json; charset=UTF-8" -H "Authorization:Basic ${basic_auth}" "${GRIDDB_WEBAPI_URL}/containers/${CONTAINER_NAME}/rows" -d @- -w "\n%{response_code}" --proxy "http://${PROXY_SERVER}/"
	fi
}
# Function to handle data import and log results
function data_import() {
	response=$(send_request "$1")

	response_body=$(echo "${response}" | sed "$ d")
	response_status=$(echo "${response}" | tail -n 1)

	if [ "${response_status}" == "200" ] ; then
		count=$(echo $response_body | jq -r '.count')
		echo "${CONTAINER_NAME}:${count} rows imported."
		log_info "${CONTAINER_NAME}:${count} rows imported."
	else
		echo "WebAPI call failed."
		echo "Response Status : ${response_status}"
		echo "Response Body : ${response_body}"
		log_error "[$response_status]$response_body"
		log_info "griddbCloudDataImport End"
		echo "Processing ends."

		return 1
	fi
}

# Argument reading
# Read command-line arguments: container name and file path
if [ $# -eq 2 ]; then
	CONTAINER_NAME=$1
	FILE_PATH=$2
else
	echo "Usage: griddbCloudDataImport.sh [CONTAINER_NAME] [FILE_PATH]"
	exit 1
fi
# Validate SKIP_HEADER_ROWS is non-negative
if [ "$SKIP_HEADER_ROWS" -lt 0 ]; then
	echo "Please specify a non-negative integer for [SKIP_HEADER_ROWS]"
	exit 1
fi
# Validate SPLIT_ROWS is greater than zero
if [ "$SPLIT_ROWS" -le 0 ]; then
	echo "Plese specify a positive integer greater than 0 for [SPLIT_ROWS]"
	exit 1
fi

# The constants below do not need to be changed.
# Extract file extension from input file path
readonly extension=$(basename "$FILE_PATH" | awk -F. '{print tolower($NF)}')
# Generate base64-encoded authentication string
readonly basic_auth=$(echo -n "${GRIDDB_USER}:${GRIDDB_PASS}" | base64)

# Main
# CSV file processing block
if [ "$extension" == "csv" ]; then
	echo "Start processing."
	log_info "------------------------------------------------------------"
	log_info "griddbCloudDataImport Start CONTAINER_NAME:${CONTAINER_NAME} FILE_PATH:${FILE_PATH}"

	# Check if input file exists
	if [ ! -e "${FILE_PATH}" ]; then
		echo "[${FILE_PATH}] does not exist."
		log_error "[${FILE_PATH}] does not exist."
		log_info "griddbCloudDataImport End"
		echo "Processing ends."
		exit 1
	fi

	# Check if input file is readable
	if [ ! -r "${FILE_PATH}" ]; then
		echo "[${FILE_PATH}] is not readable."
		log_error "[${FILE_PATH}] is not readable."
		log_info "griddbCloudDataImport End"
		echo "Processing ends."
		exit 1
	fi

	# Check if the file starts with a UTF-8 BOM and remove it
	if head -c 3 "$FILE_PATH" | od -An -tx1 | grep -q 'ef bb bf'; then
		echo "Removing BOM from $FILE_PATH"
		sed -i '1s/^\xEF\xBB\xBF//' "$FILE_PATH"
	fi

	tmp_dir="griddbCloudDataImport_chunks"

	tmp_file_name="${tmp_dir}/griddbCloudDataImport_csv_tmp_chunk_"

	if [ -n "${TEMP_FILE_PATH}" ]; then
		tmp_dir=$(echo "${TEMP_FILE_PATH}" | sed 's:/*$::')/griddbCloudDataImport_chunks
		mkdir -p "${tmp_dir}"
		tmp_file_name="${tmp_dir}/griddbCloudDataImport_csv_tmp_chunk_"
	else
		mkdir -p "${tmp_dir}"
	fi

	# Split the input file into smaller chunks to handle large data efficiently
	tail -n +"$((SKIP_HEADER_ROWS + 1))" "${FILE_PATH}" | split -l ${SPLIT_ROWS} - "$tmp_file_name"

	has_file=$(find "${tmp_dir}" -type f -print -quit | grep -q . && echo true || echo false)

	if [ "${has_file}" = "true" ]; then
		for file in ${tmp_file_name}*; do
			request_body=$(cat "$file" | jq -R -s '[split("\n") | .[] | select(length > 0) | split(",") | map(gsub("\""; ""))]')
			data_import "$request_body"
			if [ $? -ne 0 ]; then
				break
			fi
			# Clean up temporary files after each batch to avoid disk overflow
			rm -f "$file"
		done
	fi

	# Clean up temporary files after each batch to avoid disk overflow
	if [[ "${tmp_dir}" == *"griddbCloudDataImport_chunks"* ]] && [[ "${tmp_file_name}" == *"griddbCloudDataImport_csv_tmp_chunk_"* ]]; then
		rm -f "${tmp_file_name}"*
		rm -r "${tmp_dir}"
	fi

	log_info "griddbCloudDataImport End"
	echo "Processing ends."

# JSON file processing block
elif [ "$extension" == "json" ]; then
	echo "Start processing."
	log_info "------------------------------------------------------------"
	log_info "griddbCloudDataImport Start CONTAINER_NAME:${CONTAINER_NAME} FILE_PATH:${FILE_PATH}"

	# Check if input file exists
	if [ ! -e "${FILE_PATH}" ]; then
		echo "[${FILE_PATH}] does not exist."
		log_error "[${FILE_PATH}] does not exist."
		log_info "griddbCloudDataImport End"
		echo "Processing ends."
		exit 1
        fi
	
	# Check if input file is readable
	if [ ! -r "${FILE_PATH}" ]; then
		echo "[${FILE_PATH}] is not readable."
		log_error "[${FILE_PATH}] is not readable."
		log_info "griddbCloudDataImport End"
		echo "Processing ends."
		exit 1
	fi

	# Check if the file starts with a UTF-8 BOM and remove it
	if head -c 3 "$FILE_PATH" | od -An -tx1 | grep -q 'ef bb bf'; then
		echo "Removing BOM from $FILE_PATH"
		sed -i '1s/^\xEF\xBB\xBF//' "$FILE_PATH"
	fi

	tmp_dir="griddbCloudDataImport_chunks"

	tmp_file_name="${tmp_dir}/griddbCloudDataImport_json_tmp_chunk_"

	if [ -n "${TEMP_FILE_PATH}" ]; then
		tmp_dir=$(echo "${TEMP_FILE_PATH}" | sed 's:/*$::')/griddbCloudDataImport_chunks
		mkdir -p "${tmp_dir}"
		tmp_file_name="${tmp_dir}/griddbCloudDataImport_json_tmp_chunk_"
	else
		mkdir -p "${tmp_dir}"
	fi

	echo "json analysis Start."
	# Validate JSON format using jq
	jq empty "${FILE_PATH}"
	if [ $? -ne 0 ]; then
		echo "[$FILE_PATH] has an incorrect JSON format."
		log_error "[$FILE_PATH] has an incorrect JSON format."
		log_info "griddbCloudDataImport End"
		echo "Processing ends."
		exit 1
	fi

	json=$(cat "${FILE_PATH}")
	target_key=""

	# Detect known keys in JSON structure
	if echo "$json" | grep -q '"data":'; then
		target_key="data"
	elif echo "$json" | grep -q '"results":'; then
		target_key="results"
	elif echo "$json" | grep -q '"rows":'; then
                target_key="rows"
	elif echo "$json" | grep -q '"row":'; then
                target_key="row"
	else
		target_key=""
	fi

	if [ -n "$target_key" ]; then
		# Find the path to the target key in JSON
		key_path=$(jq -r --arg target_key "$target_key" 'paths | select(.[-1] == $target_key) | map(tostring) | join(".")' "${FILE_PATH}")
		key_array=($key_path)
		echo "json analysis Complete."
		for jqkey in "${key_array[@]}"; do
			# Convert dot-separated path into jq-compatible format
			# Replace numeric segments with [index] and keep string segments as .key
			key=$(echo "$jqkey" | awk -F. '{for(i=1;i<=NF;i++){if($i ~ /^[0-9]+$/){printf("[%s]", $i)}else{printf(".%s", $i)}}}')

			# Construct the jq filter to extract array elements under the target key
			if [[ "$key" == .* ]]; then
				jq_filter="$key[]"
			else
				jq_filter=".$key[]"
			fi

			# Split the input file into smaller chunks to handle large data efficiently
			jq -c "$jq_filter" "${FILE_PATH}" | split -l ${SPLIT_ROWS} - "$tmp_file_name"

			has_file=$(find "${tmp_dir}" -type f -print -quit | grep -q . && echo true || echo false)

			if [ "${has_file}" = "true" ]; then
				for file in ${tmp_file_name}*; do
					request_body=$(jq -s '.' "$file")
					data_import "$request_body"
					if [ $? -ne 0 ]; then
						break
					fi
					# Clean up temporary files after each batch to avoid disk overflow
					rm -f "$file"
				done
			fi
		done
	else
		echo "json analysis Complete."

		# Split the input file into smaller chunks to handle large data efficiently
		jq -c ".[]" "${FILE_PATH}" | split -l ${SPLIT_ROWS} - "$tmp_file_name"

		has_file=$(find "${tmp_dir}" -type f -print -quit | grep -q . && echo true || echo false)

		if [ "${has_file}" = "true" ]; then
			for file in ${tmp_file_name}*; do
				request_body=$(jq -s '.' "$file")
				data_import "$request_body"
				if [ $? -ne 0 ]; then
					break
				fi
				# Clean up temporary files after each batch to avoid disk overflow
				rm -f "$file"
			done
		fi
	fi

	# Clean up temporary files after each batch to avoid disk overflow
	if [[ "${tmp_dir}" == *"griddbCloudDataImport_chunks"* ]] && [[ "${tmp_file_name}" == *"griddbCloudDataImport_json_tmp_chunk_"* ]]; then
		rm -f "${tmp_file_name}"*
		rm -r "${tmp_dir}"
	fi

	log_info "griddbCloudDataImport End"
	echo "Processing ends."
else
	echo "Please specify .csv or .json for [FILE_PATH]."
	exit 1
fi
