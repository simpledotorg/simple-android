[![Build Status](https://app.bitrise.io/app/db9b195f645cfed7/status.svg?token=0UVLxgCzsz75d21FUnkfhg&branch=master)](https://www.bitrise.io/app/db9b195f645cfed7)

# Simple

An Android app for recording blood pressure measurements.

## Downloading a build from CI

We use Bitrise for continuous integration. It is configured to build a binary every time a commit is pushed to the repository. These binaries can be found by navigating [to this page](https://www.bitrise.io/app/db9b195f645cfed7#/builds_) and opening the details of any build. The details page will contain a generated APK called `app-debug.apk` under the "Apps & Artifacts" tab.

## Building manually

1. Clone the project using git.
2. Install [Android Studio](https://developer.android.com/studio/install#mac)
3. Create a file called `local.properties` in the root directory with the following content:

```
## This file does *NOT* get checked into your VCS, as it
## contains information specific to your local configuration.

# Location of the SDK. This is only used by Gradle.
sdk.dir=/Users/{your-username}/Library/Android/sdk
```

1. Replace `{your-username}` in `local.properties` with your actual username.
2. Run `~/Library/Android/sdk/tools/bin/sdkmanager --licenses` and accept licenses related to the SDK.
3. Build a debug variant of the binary (APK) by running `./gradlew assembleDebug` in the project directory. The generated binary will be found at `{project-directory}/app/build/outputs/apk/debug/app-debug.apk`.

When building for the first time, gradle will download all dependencies so it'll take a few minutes to complete. Subsequent builds will be faster.

Once the APK is generated, there are two ways for installing it:

## Installing

#### a) By manually transfer the binary to a phone

Android by default blocks installation of apps from outside the Play Store. To enable it, go to phone `Settings > Security` and enable `Unknown sources`. If you don't have this setting (which is the case starting from Android Oreo), your phone should automatically open this setting when you try installing the app. Once that's done, just send the APK to your phone through email, Dropbox or any other medium of your choice.

#### b) Over a USB cable

This setup requires some initial setup, but is recommended for multiple installs. To do so, `USB debugging` needs to be enabled in phone settings. The steps for finding this setting varies with manufacturers.

- If your phone settings has a search option, try searching for `Build number`.
- If search is unavailable, try navigating to `Settings > System > About phone`. Scroll to the bottom to find `Build number`.
- Tap on `Build number` for 5 times until you see a message saying "You are now a developer!".
- Go back to phone settings. A new setting group called  `Developer options `will now be available.
- Open `Developer options` and enable `USB debugging`.

The binary can now be installed on the phone by running:

```
~/Library/Android/sdk/platform-tools/adb install {path to apk}
```

If `adb` fails with a `no devices/emulators found` error, it is possible that the device is connected to the computer in charging only mode. In this case, you should see a notification on the device to change this to debugging.

## Build and deploy Simple Server

Follow the [simple-server instructions](https://github.com/simpledotorg/simple-server/blob/master/README.md).

## Miscellaneous

#### Java 8's date and time

Clinic uses [lazythreetenbp](https://github.com/gabrielittner/lazythreetenbp) for working with date and time. Due to some limitations, the IDE does not know how to download its sources. As a work around, the sources can be downloaded from [the maven repository](http://search.maven.org/#search%7Cga%7C1%7Cthreetenbp) and manually attached to Android Studio.

#### Syncing of patients

[WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) is used for periodic scheduling syncing of patients. For debugging the state of the jobs, use this command:

```
adb shell dumpsys jobscheduler | grep org.simple.clinic
```

#### Android tests

When compiling the project using Android Studio's `Make Project` option, it may not build files or run annotation processors inside the `androidTest` package. This can cause confusion when changes are made to android tests. As a workaround, `androidTest` can be compiled from the command line manually,

```
./gradlew assembleAndroidTest
```

#### SQLite Spellfix
We use [sqlite-android](https://github.com/requery/sqlite-android) because it gives us the ability to load SQLite extensions, which we use specifically for loading [spellfix1](https://www.sqlite.org/spellfix1.html).

We don't expect to change the SQLite version (we are currently using 3.24.0) often, but whenever we do, we need to recompile the version of `spellfix1` for that SQLite version. The steps for doing so are described below:

- In the [SQLite download page](https://sqlite.org/download.html), there is a section at the bottom which lists the source code mirrors. Navigate to any one of them, click on 'Tags', and select the **SAME** version of `sqlite-android` that we are upgrading to, and download a zip of the source code.
- Unzip the source code, navigate to `ext/misc` and copy the `spellfix.c` file to the `libspellfix/src/main/jni` directory.
- On the `sqlite-android` repo, in the `build.gradle` file of the [library module](https://github.com/requery/sqlite-android/blob/master/sqlite-android/build.gradle), there is an extension property that indicates the download link of the SQLite source code which it is built from. Download it, and unzip the file.
- Copy the `sqlite3.h` and `sqlite3ext.h` header files from the unzipped source directory to the `libspellfix1/src/main/jni` directory.
- Run the command `./gradlew compileSpellfix` which will compile `spellfix1` and copy the built native libs to the `jniLibs` directory in the main app src directory.
