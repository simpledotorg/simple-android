readonly GIT_HAS_NO_CHANGES=0 # 0 = clean, 1 = dirty

git config user.name "$USER_NAME"
git config user.email "$USER_EMAIL"

# Following git command checks the git diff of staged files
# and assigns the exit code to GIT_DIFF_STATUS variable.
git diff --quiet; GIT_DIFF_STATUS=$?

# If the GIT_DIFF_STATUS is 0 (GIT_HAS_NO_CHANGES)
# then we can rebase this branch onto master and push it
if [[ ${GIT_DIFF_STATUS} -eq ${GIT_HAS_NO_CHANGES} ]]; then
  git rebase master
  git push -f

exit 0
fi
