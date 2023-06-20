#!/bin/bash

herokuTeamName="resolvetosavelives"
herokuAppName=${1}
herokuApiKey=${2}
simpleServerBranch=${3}
serverAppDirectory=${4}
androidAppDirectory=${5}
encodedHerokuEnvProperties=${6}
decodedHerokuEnvProperties=$(echo $encodedHerokuEnvProperties | base64 --decode)

echo "App URL: $(heroku apps:info --app "${herokuAppName}" --json | jq -r '.app.web_url')"

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

  pip3 install requests==2.28.2
  $androidAppDirectory/.github/scripts/server_heroku_env_setup.py $serverAppDirectory/app.json $herokuAppName $herokuApiKey "$decodedHerokuEnvProperties"

  heroku labs:enable user-env-compile
  heroku stack:set --app=$herokuAppName heroku-20
  heroku addons:create --app=$herokuAppName --wait --name="${herokuAppName}-redis" heroku-redis:mini
  heroku addons:create --app=$herokuAppName --wait --name="${herokuAppName}-postgres" heroku-postgresql:mini
  heroku buildpacks:add --index 1 --app=$herokuAppName heroku/nodejs
  heroku buildpacks:add --index 2 --app=$herokuAppName heroku/ruby
  heroku buildpacks:add --index 3 --app=$herokuAppName https://github.com/weibeld/heroku-buildpack-run.git
fi

echo "Starting the Simple server on Heroku"
herokuGitUrl="https://heroku:${herokuApiKey}@git.heroku.com/${herokuAppName}.git"
(cd $serverAppDirectory && git push $herokuGitUrl ${simpleServerBranch}:master)
resultOfServerPush=$?

resultOfSeedDataSetup=0
if [ $serverAppAlreadyExists = false ]; then
  echo "Setting up initial seed data"
  (cd $serverAppDirectory && heroku run rails db:structure:load:with_data db:seed)
  resultOfSeedDataSetup=$?
fi

echo "App URL: $(heroku apps:info --app "${herokuAppName}" --json | jq -r '.app.web_url')"
echo "heroku_app_url=$(heroku apps:info --app "${herokuAppName}" --json | jq -r '.app.web_url')" >> "$GITHUB_OUTPUT"

echo "Result of starting server: ${resultOfServerPush}, seed data push ${resultOfSeedDataSetup}"

finalExitCode=$((resultOfServerPush | resultOfSeedDataSetup))

echo "Set up server result: ${finalExitCode}"
exit ${finalExitCode}
