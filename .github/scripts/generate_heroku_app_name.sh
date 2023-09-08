#!/bin/bash

herokuTeamName="resolvetosavelives"
# Extract the PR number from the GitHub ref ("refs/pull/<pr number>/merge"|"refs/heads/release/2021-08-20")
echo "GitHub reference: ${1}"
serverAppDirectory=${2}

if [[ ${1} == *"/release/"* ]]; then
  herokuAppName="simple-mob-rel-$(date +"%H%M")"
else
  name=$(cut -d"/" -f3 <<< ${1})
  herokuAppNameFull="simple-mob-pr-$name"
  herokuAppName=${herokuAppNameFull:0:29}
fi

echo "Heroku app name: ${herokuAppName}"

echo "heroku_app_name=$herokuAppName" >> $GITHUB_OUTPUT

cho "Checking if ${herokuAppName} exists in team ${herokuTeamName}"

existingAppName=$(heroku apps --team=${herokuTeamName} | grep "$herokuAppName")

serverAppAlreadyExists=false

if [ -n "${existingAppName}" ]; then
  echo "Found existing app: ${herokuAppName}"
  serverAppAlreadyExists=true
fi

if [ $serverAppAlreadyExists = false ]; then
  (cd $serverAppDirectory && heroku apps:create --team $herokuTeamName $herokuAppName)
fi

echo "server_app_already_exists=$serverAppAlreadyExists" >> "$GITHUB_OUTPUT"

echo "heroku_app_url=$(heroku apps:info --app "${herokuAppName}" --json | jq -r '.app.web_url')" >> "$GITHUB_OUTPUT"

