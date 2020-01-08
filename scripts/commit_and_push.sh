#!/usr/bin/env bash

readonly GIT_HAS_CHANGES=1 # 0 = clean, 1 = dirty

USER_NAME="$1"
USER_EMAIL="$2"

git config --global user.name "$USER_NAME"
git config --global user.email "$USER_EMAIL"

git diff --quiet; GIT_DIFF_STATUS=$?

if [[ ${GIT_DIFF_STATUS} -eq ${GIT_HAS_CHANGES} ]]; then
    DAY=$( date +%b-%d-%Y )
    BRANCH_NAME="ga/$DAY/code-formatting"
    echo "Branch name is $BRANCH_NAME"

    git checkout -b "$BRANCH_NAME"
    git commit -am "Code formatting fixes"
    git push -u origin "$BRANCH_NAME"

    echo "Git changes pushed to $BRANCH_NAME"
else
    echo "No git changes"
fi
