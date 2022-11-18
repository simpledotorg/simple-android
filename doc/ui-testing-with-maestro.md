

# UI testing with Maestro

We cannot detect crashes that occur while navigating through the screens, opening the app, incompatible image assets etc with the help of Unit or Integration tests. Opening and navigating through the app manually is the only way to identify them. Crashes like these can be detected via UI tests where we write tests for a set of screen interactions. We use Maestro for UI testing in Simple as it offers a single binary tool that works anywhere with declarative yet robust syntax and has a quick learning curve.

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

Below is an example of how to test login flow in Simple. Flow can be found [here](https://github.com/simpledotorg/simple-android/blob/master/maestroUiFlows/login_flow.yaml)

    appId: org.simple.clinic.staging
    ---
    - clearState
    - launchApp
    - tapOn: "Next"
    - tapOn: "Get started"
    - tapOn:
        below:
          id: "select_country_title"
    - tapOn:
        below:
          id: "select_state_title"
    - inputText: ${number}
    - tapOn: "Next"
    - assertVisible:
        text: "Your security PIN"
    - inputText: ${pin}
    - tapOn: "Enter code"
    - inputText: ${otp}
    - tapOn: "Got it"


## Running a Flow

Flow can be run using the `maestro test` command.

    maestro test login_flow.yaml

Params to the flow can be given from the terminal as given below

    maestro test -e number=0123456789 -e pin=0000 -e otp=000000 uiFlows/login_flow.yaml
