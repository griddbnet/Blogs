#!/bin/bash

file=$1

#EXAMPLE
#runCurl() {
# curl 'https://cloud57.griddb.com/mfcloud57/dbaas/web-api/contracts/m01wc1a/access-list' -X POST -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:133.0) Gecko/20100101 Firefox/133.0' -H 'Accept: application/json, text/plain, */*' -H 'Accept-Language: en-US,en;q=0.5' -H 'Accept-Encoding: gzip, deflate, br, zstd' -H 'Content-Type: application/json;charset=utf-8' -H 'Access-Control-Allow-Origin: *' -H 'Authorization: Bearer eyJ0eXAiOiJBY2Nlc3MiLCJIUzI1NiJ9.eyJzdWIiOiJkMTg4NjlhZC1mYjUxLTQwMWMtOWQ0Yy03YzI3MGNkZTBmZDkiLCJleHAiOjE3MzEwMTEyMTMsInJvbGUiOiJBZG1pbiIsInN5c3RlbVR5cG-Nu8m8mJbsp6dKABjJDBjQDdc9aRLffTlTcVM' -H 'Origin: https://cloud5197.griddb.com' -H 'Connection: keep-alive' -H 'Referer: https://cloud97.griddb.com/mfcloud57/portal/' -H 'Sec-Fetch-Dest: empty' -H 'Sec-Fetch-Mode: cors' -H 'Sec-Fetch-Site: same-origin' -H 'Priority: u=0' -H 'Pragma: no-cache' -H 'Cache-Control: no-cache' --data-raw $1
#}

runCurl() {
    <PASTE VALUE HERE AND REMOVE LAST IP ADDRESS IN SINGLE QUOTES> $1
}

while IFS= read -r line; do
    for ip in ${line//,/ }; do
        echo "Whitelisting IP Address: $ip"
        runCurl $ip
    done
done < "$file"