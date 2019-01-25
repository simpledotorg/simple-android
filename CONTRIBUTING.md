# Contributing to Simple Android

Features that we want to build for Simple are available as "user stories" on our [Pivotal Tracker](https://www.pivotaltracker.com/n/projects/2184102) board. The stories in the Backlog are (usually) triaged and prioritised according to their feasibility and usefulness to the project. 

To pick up a story,

- Speak to the person assigned to the story to get context/clarifications
- Update the story with any relevant information so that other members also see it, if need be. Detailed descriptions are useful for code reviewers as well as testers
- If the story requires changes to the API endpoints or something new entirely, discuss with the backend team by tagging them in the story
- Add acceptance criteria to the story
- Break the story down into multiple tasks to help with estimation.
- Estimate the story between 1, 2 and 3 points depending upon their complexity. A 1-point story could refer to a task that can be done within a day. A 3-point story would take multiple days 
- If a story is very complex, split it into multiple stories 
- Story states
  - Started: development in progress
  - Finished: development complete, PR in review
  - Delivered: PR merged, QA pending
  - Accepted: ready for deploy
  - Rejected:
    - update acceptance criteria
    - restart, update PR 

### Code style and checks

When submitting code, please ensure that you follow our existing code conventions and style in order to keep the code as readable as possible. 

This project includes a gradle task that automatically installs a git hook to run lint and tests before every commit. We don't have an auto code formatter in place yet, but we do expect the code to be formatted as per our code-style before raising a pull request. The code-style can be found inside `/quality` folder and can be imported into Android Studio through `Preferences > Editor > Code Style`.

### Git workflow

Simple follows a variant of [trunk based development](https://trunkbaseddevelopment.com/) by having only one "master" branch, and new changes developed and deployed behind feature flags. Instead of creating long-lived feature branches, we create daily branches and merge to master every day. 

### Automated tests

To rely on manual testing as little as possible, new changes must include unit tests (that run on the JVM) or instrumented tests (that run on an emulator or real device). The existing test suite can be run using,

`./gradlew testQaDebug connectedQaDebugAndroidTest`

### Submitting a pull request

- Link to the Pivotal Tracker story
- Describe the changes with text and images
- More detail is always better!
- Ensure tests for all acceptance criteria

