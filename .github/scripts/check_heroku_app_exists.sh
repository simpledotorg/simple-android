#!/bin/bash

herokuTeamName=${1}
herokuAppName=${2}

echo "Checking if ${herokuAppName} exists in team ${herokuTeamName}"

existingAppName=$(heroku apps --team=${herokuTeamName} | grep "$herokuAppName")

echo "Found [${existingAppName}]."

if [ -n "${existingAppName}" ]; then
  echo "Found"
  echo "::set-output name=heroku_app_exists::true"
else
  echo "Not Found"
  echo "::set-output name=heroku_app_exists::false"
fi
