#!/bin/bash

herokuTeamName=${1}
herokuAppName=${2}

echo "Checking if ${herokuAppName} exists in team ${herokuTeamName}"

existingAppName=$(heroku apps --team=${herokuTeamName} | grep "$herokuAppName")

if [ -n "${existingAppName}" ]; then
  echo "::set-output name=heroku_app_exists::true"
else
  echo "::set-output name=heroku_app_exists::false"
fi
