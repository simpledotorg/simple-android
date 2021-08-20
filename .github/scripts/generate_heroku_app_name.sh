#!/bin/bash

# Extract the PR number from the GitHub ref ("refs/pull/<pr number>/merge")
echo "Arg: ${1}"
gitRef=$(cut -d"/" -f3 <<< ${1})
echo "GiHub ref: ${gitRef}"

herokuAppName="simple-android-review-app-$gitRef"
echo "Heroku app name: ${herokuAppName}"

echo "::set-output name=heroku_app_name::${herokuAppName}"
