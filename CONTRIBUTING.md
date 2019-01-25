# Contributing to Simple Android

Features that we want to build for Simple are available as "user stories" on [Pivotal](https://www.pivotaltracker.com/n/projects/2184102). They're triaged and prioritised according to their feasibility and userfulness learned during our user studies. 

To pick up a story,

- Speak to the person assigned to the story to get context/clarifications
- Update the story with any relevant information that other members can see. Detailed description are useful for code reviewers as well as testers.
- If the story needs an API change, discuss with the backend team
- Add acceptance criteria to the story
- Break the story down to multiple tasks to help with estimation.
- Estimate the story between 1, 2 and 3 points depending upon their complexity. A 1-point story could refer to a task that can be done within a day. A 3-point story would take multiple days. 
- If a story is very complex, consider splitting it into multiple stories 
- Story states
  - Started: dev in progress
  - Finished: dev complete, PR in review
  - Delivered: PR merged, QA pending
  - Accepted: ready for deploy
  - Rejected:
    - update acceptance criteria
    - restart, update PR 


### Setup codestyle and checks

TODO


### Running Tests

New changes ideally include automated tests that can run on the JVM or on a real device.

TODO


### Submitting a pull request

- Link to the pivotal story
- Optionally describe the changes with text and images
- Ensure tests for all acceptance criteria
