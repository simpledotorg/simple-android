# Releasing the Simple Android app

Our releases happens at regular intervals. For now, this interval is one (sometimes two) week, and Monday is release day.

Our integration branch is `master`: this is where PRs are merged, and release branches are made from. All release branches have a one-week holding period, during which we manually verify the app before it goes live on the Play Store.

## Steps to release

The release manager must do two things on every release day:

### Prepare next release

1. At the end of the working day, a branch is cut from `master` named in a specific format. 
   - The format has two parts, an identifier that it is a _release_ branch, and the date on which the branch is scheduled to go live. 
   - For example, the release branch cut on November 7th 2018 will be named `release/2018-11-21` to indicate that it is the release which is scheduled to go live on 21st November 2018.
    
2. Once the release branch has been made, the `demo` variant is built from this branch and released to [Simple Demo](https://play.google.com/store/apps/details?id=org.simple.clinic.staging) on the Play Store.
   - There is a workflow on Bitrise to do this.
   - The Demo app is open to all. Anyone may download and test new features that will soon be available on production.
   - No data on the Demo app is real patient data, everything is fake. People are encouraged to add their own data and try things out.
  
### Fix issues

1. If someone discovers a problem in the upcoming release, it must be fixed and merged into `master`, and then cherry-picked into the release branch.
2. Only a few categories of issues are eligible for cherry-picking: bug fixes and language/text changes. All other types of issues will have to wait till the next release goes out. 
3. Whenever a commit is cherry-picked from `master` onto the release branch, it should be done with the `-x` flag to record the SHA1 hash of the original commit on `master`. 


        $ git cherry-pick -x a986fb4
        $ git show 

        commit 5eaf72b8ee1865595df49ecfea20115b8454f639
        Author: Ajay Kumar <not@obvious.in>
        Date:   Fri Mar 22 19:42:07 2019 +0530

        Show keyboard automatically when update phone dialog is shown
    
        (cherry picked from commit a986fb4)


### Deploy to Play Store

1. The branch that has been in QA since the last release day is assumed to be ready for publishing.
2. Bitrise has a workflow which builds and deploys APKs from any arbitrary branch and pushes it to the Play Store as a release draft. This build is triggered on the release branch.
3. Once the build completes, a production release must be created on the Google Play Console, and the release notes should be copied from the CHANGELOG.md file.
4. Finally, the pending-release section in CHANGELOG.md must be updated on `master`, with the version name of the APK on the Play Store.
