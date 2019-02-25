
# How to test the Simple App

## Test in Sandbox

0. Have a look at the [GitHub Project Wall](https://github.com/orgs/resolvetosavelives/projects/1?fullscreen=true). The "QA" column contains the cards which are dev-complete and bug-free. Any card with a label of `qa` or `qa-passed` can be experimented with. `qa-passed` means the card has passed an initial QA by Steven. Viewing and submitting bugs is explained in Step 2.

1. [Download the Sandbox app from the Play Store](https://play.google.com/apps/testing/org.simple.clinic.sandbox)

2. Click through the application as per any card you found in Step 0 with `qa` or `qa-passed` on it.

  - Read the `As a ... I can ... So that...` introduction to get an idea of what the card represents.
  - Under this, every card should have a `## Bugs` section in the Description. If you find a bug, add it to the bottom of the list with `- [ ] <your bug's description>` and it will show up as a task on the project wall. If the bug is large (redesign or a dev says it will take time), create an issue for it.
  - You can also try cards in `Doing` and `Final Iteration` with the `qa-needs-fix` label on them but be aware of the bugs listed on the cards already.

3. Visit http://api-sandbox.simple.org to test View/Create/Update/Delete of both Users and Facilities. This is helpful for creating sample sandbox data. Any card with the description `Admin can...` refers to these raw user interfaces.
