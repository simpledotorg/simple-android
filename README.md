[![Build Status](https://app.bitrise.io/app/db9b195f645cfed7/status.svg?token=0UVLxgCzsz75d21FUnkfhg&branch=master)](https://www.bitrise.io/app/db9b195f645cfed7)
[![pullreminders](https://pullreminders.com/badge.svg)](https://pullreminders.com?ref=badge)

# Simple

An Android app for recording blood pressure measurements.

## How to build

1. Clone the project using git.
2. Install [Android Studio](https://developer.android.com/studio/).
3. Import the project into Android Studio.

When building for the first time, gradle will download all dependencies so it'll take a few minutes to complete. Subsequent builds will be faster.

## Code styles
The code styles which the project uses have been exported as an IntelliJ code style XML file and are saved as `quality/code-style.xml`.

#### Importing the code styles
1. Open the Android Studio preferences page, and navigate to Editor -> Code Style.
2. Click on the gear/settings button next to the "Scheme" label.
3. In the drop-down menu, select "Import scheme".
4. In the file picker, navigate to  `<project>/quality/code-style.xml`.
5. Import the `Simple` scheme into the IDE and set it as the project code style.

## Build and deploy Simple Server

Simple Server is in a separate repository, and you should follow the [instructions there](https://github.com/simpledotorg/simple-server/blob/master/README.md).
