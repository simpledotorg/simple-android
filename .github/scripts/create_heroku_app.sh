#!/bin/bash

herokuTeamName="resolvetosavelives"
herokuAppName=${1}
serverAppDirectory=${2}

echo "Checking if ${herokuAppName} exists in team ${herokuTeamName}"

existingAppName=$(heroku apps --team=${herokuTeamName} | grep "$herokuAppName")

serverAppAlreadyExists=false

if [ -n "${existingAppName}" ]; then
  echo "Found existing app: ${herokuAppName}"
  serverAppAlreadyExists=true
fi

if [ $serverAppAlreadyExists = false ]; then
  echo "Setting up server app [$herokuAppName]"
  (cd $serverAppDirectory && heroku apps:create --team $herokuTeamName $herokuAppName)
  heroku pipelines:add --app=$herokuAppName --stage=staging simple-android-review
fi

echo "heroku_app_name=${herokuAppName}" >> "$GITHUB_OUTPUT"
echo "heroku_app_url=$(heroku apps:info --app "${herokuAppName}" --json | jq -r '.app.web_url')" >> "$GITHUB_OUTPUT"
echo "heroku_app_exists=${serverAppAlreadyExists}" >> "$GITHUB_OUTPUT"
