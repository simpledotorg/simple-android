![Build Status](https://github.com/simpledotorg/simple-android/workflows/CI/badge.svg)
[![pullreminders](https://pullreminders.com/badge.svg)](https://pullreminders.com?ref=badge)

# Simple

An Android app for recording blood pressure measurements.

## Pre-requisites

The application currently requires JDK 11 to build. If you already have JDK 11 installed, skip this step.

**Check if the right JDK is already available**

Run the command `java -version`. If you have the right version of the JDK installed, you should see something like:
```sh
openjdk version "11.0.10" 2021-01-19
OpenJDK Runtime Environment AdoptOpenJDK (build 11.0.10+9)
OpenJDK 64-Bit Server VM AdoptOpenJDK (build 11.0.10+9, mixed mode)
```

If this command has an error, or shows a different version, you can follow the instructions below to install the JDK.

**Install the JDK**

We recommend using [jEnv](https://www.jenv.be/) to manage your JDK installations. Here are instructions to setup a working JDK 1.8 installation (macOS only):

1. Setup up [Homebrew](https://brew.sh/).

2. Install `jEnv` using Homebrew.
```sh
brew install jenv
```

3. Add the following lines to your shell configuration file (`~/.bash_profile` if you're using bash, or `~/.zshrc` if you're using zsh).
```sh
export PATH="$HOME/.jenv/bin:$PATH"
eval "$(jenv init -)"
```

4. Once this is done, you'll need to restart the terminal or reload the configuration file in order for the `jenv` command to be recognised.
```sh
source <path to shell configuration file>
```

5. Install the JDK using Homebrew.
```sh
brew tap AdoptOpenJDK/openjdk
brew cask install adoptopenjdk11
```

6. Add the installed JDK to `jEnv`
```sh
jenv add /Library/Java/JavaVirtualMachines/adoptopenjdk-11.jdk/Contents/Home
```

7. Run the command `jenv versions`. You should see something like:
```sh
  system
  11
* 11.0
  11.0.10
  openjdk64-11.0.10
```

## How to build

**Clone the project using git.**

Run the following command in a terminal.

 ```
 $ git clone git@github.com:simpledotorg/simple-android.git
 ```

**Install Android Studio**

Download and install Android Studio from [their website](https://developer.android.com/studio/).

**Import the project into Android Studio.**

When Android Studio starts up, it will prompt you to create a new project or import an existing project. Select the
option to import an existing project, navigate to the `simple-android` directory you cloned earlier, and select it.

When building for the first time, gradle will download all dependencies so it'll take a few minutes to complete.
Subsequent builds will be faster.

## Running locally

The Simple App can be run locally on an Android emulator using Android Studio. To do this,

**Install the NDK library**

The NDK library is currently required by the project to enable an SQLite extension. To install it:

* Open the SDK Manager through Tools -> SDK Manager
* Select Appearance & Behavior -> System Settings -> Android SDK in the left sidebar
* Select the SDK Tools tab in the main window
* Activate NDK (Side by Side) and click Apply

NDK will now be installed.

**Create a Run/Debug configuration**

* Open the Run/Debug configurations window through Run -> Edit Configurations ([ref](https://developer.android.com/studio/run/rundebugconfig))
* Create a new configuration using the `Android App` template
* Set the module to `app`, and finish creating the configuration

**Create a virtual device**

* Create an Android Virtual Device (AVD) using the AVD Manager, usually found in Tools -> AVD Manager. ([ref](https://developer.android.com/studio/run/managing-avds))
* Select a device and operating system
* Note: You will have to download one of the available OS options the first time you create an AVD

**Set the right build variant**

* Open the Build Variants window through View -> Tool Windows -> Build Variants, or clicking the item in the lower left
  corner of the main window
* Set the Build Variant of the app module to `qaDebug`

**Run the app**

* Click "Run", either through Run -> Run, or the green play button in the top toolbar.

## Code styles

The code styles which the project uses have been exported as an IntelliJ code style XML file and are saved as
`quality/code-style.xml`. To import them into Android Studio,

1. Open the Android Studio preferences page, and navigate to Editor -> Code Style.
1. Click on the gear/settings button next to the "Scheme" label.
1. In the drop-down menu, select "Import scheme".
1. In the file picker, navigate to  `<project>/quality/code-style.xml`.
1. Import the `Simple` scheme into the IDE and set it as the project code style.

## Tooling

An Android Studio plugin that provides some quality of life improvements like live templates can be found [HERE](https://github.com/simpledotorg/simple-android-idea-plugin).

## Building an APK with a different build variant

There are currently 2 ways to build an app pointing to different environments:

1. Changing the `qa` API URL in `gradle.properties` file to point to the environment you want. These builds will be debuggable and require us to clone the project and build it using [Android Studio](https://developer.android.com/studio). [*Warning*: These changes should not be commited back to `master` branch]
2. Use Bitrise workflows to build APKs of different build variants. These builds will not be debuggable, unless for `build-debuggable-sandbox-apk`.

## Build and deploy Simple Server

Simple Server is in a separate repository, and you should follow the [instructions there](https://github.com/simpledotorg/simple-server/blob/master/README.md).

## Execute SQL Queries

You can use [Flipper](https://fbflipper.com/) to run SQL queries on Simple:

1. Install Flipper using brew or download from their [website](https://fbflipper.com/). 
```sh 
brew install Flipper
```
2. Launch Flipper (you might have to allow Flipper to launch from System Preferences > Security > General as it’s from an unknown developer to Apple).
3. Run the Simple app in an emulator or your physical device(as Flipper loads the data from your device's local database).
4. In the Plugins section in the sidebar menu click on Disabled and enable the Database plugin.
5. Click on Databases, select `red-db` and choose whichever table’s data you want to inspect.
6. Click on SQL at the top to execute SQL queries.

## Resources

Check out the following documents for more information.

* [Quirks That You Should Probably Be Aware Of](doc/QUIRKS.md)
* [More Documentation](doc)
* [Recipes](doc/recipes.md)
