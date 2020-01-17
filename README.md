![Build Status](https://github.com/simpledotorg/simple-android/workflows/CI/badge.svg)
[![pullreminders](https://pullreminders.com/badge.svg)](https://pullreminders.com?ref=badge)

# Simple

An Android app for recording blood pressure measurements.

## How to build

1. Clone the project using git.
   ```
   $ git clone git@github.com:simpledotorg/simple-android.git
   ```
1. Install [Android Studio](https://developer.android.com/studio/).
1. Import the project into Android Studio.

When building for the first time, gradle will download all dependencies so it'll take a few minutes to complete. Subsequent builds will be faster.

## Running locally

The Simple App can be run locally on an Android emulator using Android Studio. To do this,

1. Create a Run/Debug configuration for the project. Use the `Android App` template and set the module to `app`. ([ref](https://developer.android.com/studio/run/rundebugconfig))
1. Create an Android Virtual Device (AVD) using the AVD Manager, usually found in the Tools menu ([ref](https://developer.android.com/studio/run/managing-avds))
1. Select a device and operating system. You will have to download one of the available OS options the first time you
   create an AVD.
1. Open the Build Variants window through View -> Tool Windows -> Build Variants, or clicking the item in the lower left
   corner of the main window.
1. Set the Build Variant of the app module to `qaDebug`
1. Click "Run", either through Run -> Run, or the green play button in the top toolbar.

## Code styles

The code styles which the project uses have been exported as an IntelliJ code style XML file and are saved as
`quality/code-style.xml`. To import them into Android Studio,

1. Open the Android Studio preferences page, and navigate to Editor -> Code Style.
1. Click on the gear/settings button next to the "Scheme" label.
1. In the drop-down menu, select "Import scheme".
1. In the file picker, navigate to  `<project>/quality/code-style.xml`.
1. Import the `Simple` scheme into the IDE and set it as the project code style.

## Build and deploy Simple Server

Simple Server is in a separate repository, and you should follow the [instructions there](https://github.com/simpledotorg/simple-server/blob/master/README.md).

## Resources

Check out the following documents for more information.

* [Quirks That You Should Probably Be Aware Of](doc/QUIRKS.md)
* [More Documentation](doc)
