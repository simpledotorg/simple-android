# Releasing the Simple Android app
The release happens at regular intervals. For now, this interval is one week, and Wednesday is release day.

Our integration branch is `master`: this is where PRs are merged, and releases are made. Our release train runs two weeks behind `master` which gives us enough time to QA before it goes to production.

## Steps to relase
The release manager must do two things on every release day:

### Prepare next release
1. At the end of the working day (EOD), a branch is cut from `master` named in a specific format. The format has two parts, an identifier that it is a **release** branch, and the date on which the branch is scheduled to go live. For example, the release branch cut on November 7th 2018 will be named `release/2018-11-21` to indicate that it is the release which is scheduled to go live on 21st November 2018.
2. A new section is added to the [`CHANGELOG`](https://github.com/simpledotorg/simple-android/wiki/Changelog) which will contain the release notes for the upcoming release branch. This will serve as a reference to QA for testing and for release notes when the update is live.
3. This branch will remain in QA for **one week** until release day. During this period, QA can build this branch from CI (process to build this branch will be described later) and can verify the builds.
4. Certain commits which get merged into `master` when this branch is in QA will get cherry-picked into this branch. Only a few categories of commits are eligible for being cherry-picked, examples being bug fixes and copy changes. Whenever this happens, the release section in the changelog will have to be updated. 
5. Whenever a commit is cherry-picked from `master` onto the release branch, it needs to be done with the `-x` flag to record the SHA1 hash of the commit on `master`. 

### Complete current release
1. The branch that has been in QA since the last release day is assumed to be ready for publishing.
2. The CI has a workflow which builds and deploys APKs from any arbitrary branch and pushes it to the Play Store as a release draft. This build is triggered on the release branch.
3. Once the build completes, a production release must be created on the Google Play Console, and the release notes should be copied from the CHANGELOG page on Github.
