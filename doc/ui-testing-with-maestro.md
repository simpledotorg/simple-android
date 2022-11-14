# UI testing with Maestro

##  Install the Maestro CLI

Setup Maestro CLI on the system with the [Homebrew](https://brew.sh/)

    brew tap mobile-dev-inc/tap
    brew install maestro

   ## Understating Flows

   A **Flow** is a series of steps that tells Maestro how to navigate through the application. Given below is an example from Simple, where Maestro is used to testing the login flow. Here the flow is given in the `login_flow.yaml`, this includes common commands such as

 - [`clearState`](https://maestro.mobile.dev/reference/app-files) - *clears the application state*
 - [`launchApp`](https://maestro.mobile.dev/reference/app-lifecycle) - *launches the app*
 - [`tapOn`](https://maestro.mobile.dev/reference/tap-on-view) - *taps on a view on the screen*
 - [`assertVisible`](https://maestro.mobile.dev/reference/assertions) - *asserts whether an element is visible*
 - [`inputText`](https://maestro.mobile.dev/reference/text-input) - *inputs text*

More Maestro flow commands can be found [here](https://maestro.mobile.dev/).

    appId: org.simple.clinic.staging
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
