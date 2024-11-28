#!/bin/bash

# There's a possibility the Simple Server takes a while to come on, and is not
# ready when the Android integration tests need it. This implements a wait loop
# for 5 minutes — we shouldn't be waiting for longer than this — to check if we
# can ping a particular URL on the server for a healthcheck.
# 
# A good example is to ping for the manifest and check if we get a 200
#
#     ./wait_for_simple_server.sh http://<simple-server-host>/api/manifest.json 200

# Accept two arguments without assuming sane defaults
if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <endpoint_url> <status_code>"
  exit 1
fi

endpoint=$1
desired_status=$2
timeout=300
start_time=$(date +%s)

while true; do
  status=$(curl -o /dev/null -s -w "%{http_code}\n" "$endpoint")

  if [ "$status" -eq "$desired_status" ]; then
    echo "Endpoint is up and running with status $desired_status."
    break
  else
    echo "Current status: $status. Retrying..."
    # Wait for a few seconds before retrying
    sleep 5
  fi

  current_time=$(date +%s)
  elapsed_time=$((current_time - start_time))
  if [ "$elapsed_time" -ge "$timeout" ]; then
    echo "Timeout reached. Exiting with failure."
    exit 1
  fi
done
