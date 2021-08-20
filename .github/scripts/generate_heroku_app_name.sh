#!/bin/bash

# Extract the PR number from the GitHub ref ("refs/pull/<pr number>/merge"|"refs/heads/release/2021-08-20")
echo "GitHub reference: ${1}"

if [[ ${1} == *"/release/"* ]]; then
  herokuAppName="simple-android-review-app-$(date +"%H%M")"
else
  herokuAppName="simple-android-review-app-$(cut -d"/" -f3 <<< ${1})"
fi

echo "Heroku app name: ${herokuAppName}"

echo "::set-output name=heroku_app_name::${herokuAppName}"
