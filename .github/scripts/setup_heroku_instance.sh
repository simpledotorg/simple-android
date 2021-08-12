#!/bin/bash

herokuTeamName="resolvetosavelives"
herokuAppName=${1}
herokuApiKey=${2}
serverAppDirectory=${3}
androidAppDirectory=${4}
additionalHerokuEnvProperties=${5}

echo "Checking if ${herokuAppName} exists in team ${herokuTeamName}"

existingAppName=$(heroku apps --team=${herokuTeamName} | grep "$herokuAppName")

serverAppAlreadyExists=false

if [ -n "${existingAppName}" ]; then
  echo "Found"
  serverAppAlreadyExists=true
fi

if [ $serverAppAlreadyExists = false ]; then
  echo "Setting up server app [$herokuAppName]"
  (cd $serverAppDirectory && heroku apps:create --team $herokuTeamName $herokuAppName)
  heroku pipelines:add --app=$herokuAppName --stage=staging simple-android-review

  pip3 install requests
  $androidAppDirectory/.github/scripts/server_heroku_env_setup.py $serverAppDirectory/app.json $herokuAppName $herokuApiKey $additionalHerokuEnvProperties

  heroku addons:create --app=$herokuAppName --wait --name="${herokuAppName}-redis" heroku-redis:hobby-dev
  heroku addons:create --app=$herokuAppName --wait --name="${herokuAppName}-postgres" heroku-postgresql:hobby-dev
  heroku buildpacks:add --index 1 --app=$herokuAppName heroku/nodejs
  heroku buildpacks:add --index 2 --app=$herokuAppName heroku/ruby
fi

echo "Starting the Simple server on Heroku"
herokuGitUrl="https://heroku:${herokuApiKey}@git.heroku.com/${herokuAppName}.git"
(cd $serverAppDirectory && git push $herokuGitUrl master)

if [ $serverAppAlreadyExists = false ]; then
  echo "Setting up initial seed data"
  (cd $serverAppDirectory && heroku run rails db:schema:load db:seed)
fi
