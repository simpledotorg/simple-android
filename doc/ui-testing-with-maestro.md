
# UI testing with Maestro

Simple have nurse's login flow which is a P0 flow for the app and also to move forward and register patients in the app. UI tests were necessary for login flow so that it helps us understand if the flow is working as expected and if there's no blocker to using the app. As Maestro offers a single binary tool that works anywhere with declarative yet robust syntax and has a quick learning curve, we use it for UI testing in Simple.

##  Install the Maestro CLI

Setup Maestro CLI on the system with the [Homebrew](https://brew.sh/)

    brew tap mobile-dev-inc/tap
    brew install maestro 

## Understating Flows

A **Flow** is a series of steps that tell Maestro how to navigate the application. Here are some commonly used steps in a flow.

- [`clearState`](https://maestro.mobile.dev/reference/app-files) - *clears the application state*
- [`launchApp`](https://maestro.mobile.dev/reference/app-lifecycle) - *launches the app*
- [`tapOn`](https://maestro.mobile.dev/reference/tap-on-view) - *taps on a view on the screen*
- [`assertVisible`](https://maestro.mobile.dev/reference/assertions) - *asserts whether an element is visible*
- [`inputText`](https://maestro.mobile.dev/reference/text-input) - *inputs text*

More Maestro flow commands can be found [here](https://maestro.mobile.dev/).

Here is an example of how to test login flow in Simple. Flow can be found [here](https://github.com/simpledotorg/simple-android/blob/master/maestroUiFlows/login_flow.yaml)

## Running a Flow

Flow can be run using the `maestro test` command.

    maestro test login_flow.yaml
