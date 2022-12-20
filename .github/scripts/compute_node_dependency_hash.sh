#!/bin/bash

nodeVersion=$(node -v)
npmVersion=$(npm -v)
herokuCliVersion=$(npm view heroku version)

echo "Generating cache key from dependencies [node, npm, heroku]"

stringToHash="${nodeVersion} ${npmVersion} ${herokuCliVersion}"

hashed=$(shasum <<< stringToHash | cut -f 1 -d ' ')

echo "Hashed \"${stringToHash}\" -> \"${hashed}\""

echo "node_dep_hash=$hashed" >> $GITHUB_OUTPUT
