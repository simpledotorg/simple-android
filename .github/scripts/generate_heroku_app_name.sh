#!/bin/bash

# Extract the PR number from the GitHub ref ("refs/pull/<pr number>/merge"|"refs/heads/release/2021-08-20")
echo "GitHub reference: ${1}"

if [[ ${1} == *"/release/"* ]]; then
  herokuAppName="simple-mob-rel-$(date +"%H%M")"
else
  name=$(cut -d"/" -f3 <<< ${1})
  herokuAppNameFull="simple-mob-pr-$name"
  herokuAppName=${herokuAppNameFull:0:30}
fi

echo "Heroku app name: ${herokuAppName}"

# echo "heroku_app_name=$herokuAppName" >> $GITHUB_OUTPUT
