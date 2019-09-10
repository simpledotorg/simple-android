#!/usr/bin/env bash

# This is an experimental script and has a lot of room for improvement.

propertiesFile="./local.properties"

function property {
    grep "${1}" ${propertiesFile} | cut -d'=' -f2
}

function promptForInitials {
  echo "Please enter your initials: "
  read git_branch_initials

  initialsKey=$'\ngit.branch.initials'
  echo "$initialsKey=$git_branch_initials" >> ${propertiesFile}

  echo "I will use '$git_branch_initials' as your initials. You can change it later by editing the './local.properties' file."
}

command -v git >/dev/null 2>&1 || { echo >&2 "I require git but it's not installed."; exit 1; }

branch_name=${1}

if [[ -z ${branch_name} ]]
then
  echo "usage: newbranch <branch-name>"
  exit 1
fi

git_branch_initials=$(property 'git.branch.initials')

if [[ -z ${git_branch_initials} ]]
then
  promptForInitials
fi

date_today=$(date +'%d')

month=$(date +'%B')
short_month=`echo "${month:0:3}" | tr '[:upper:]' '[:lower:]' | cut -c1-3`

desired_branch_name="${git_branch_initials}/$date_today$short_month/$branch_name"
git checkout -b ${desired_branch_name}
