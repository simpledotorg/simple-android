#!/bin/bash

docker network inspect backend

url="http://localhost:8420"
timeout_in_seconds=3600  # 60 minutes in seconds

start_time=$(date +%s)

while true; do
    response_code=$(curl -s -o /dev/null -w "%{http_code}" "$url")

    if [ "$response_code" -eq 200 ]; then
        echo "Success! Received 200 OK response."
        break
    else
        echo "Failed, got $response_code as response code. Retrying..."
        sleep 5

        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))

        if [ "$elapsed_time" -ge "$timeout_in_seconds" ]; then
            echo "Timeout reached. Exiting."
            exit 1
        fi
    fi
done
