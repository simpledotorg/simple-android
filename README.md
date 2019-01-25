[![Build Status](https://app.bitrise.io/app/db9b195f645cfed7/status.svg?token=0UVLxgCzsz75d21FUnkfhg&branch=master)](https://www.bitrise.io/app/db9b195f645cfed7)

# Simple

An Android app for recording blood pressure measurements.

## Building

1. Clone the project using git
2. Install [Android Studio](https://developer.android.com/studio/install#mac)
3. Import the project into Android Studio

When building for the first time, gradle will download all dependencies so it'll take a few minutes to complete. Subsequent builds will be faster.

## Running the app

An APK can be built and installed to a device directly from Android Studio. The device could be an emulator or a real device. Emulators come pre-configured to run binaries, but running on a real device requires enabling `USB debugging` in phone settings. The steps for finding this setting varies with manufacturers, but should be somewhat along the lines of,

- If your phone settings has a search option, try searching for `Build number`.
- If search is unavailable, try navigating to `Settings > System > About phone`. Scroll to the bottom to find `Build number`.
- Tap on `Build number` for 5 times until you see a message saying "You are now a developer!".
- Go back to phone settings. A new setting group called  `Developer options `will now be available.
- Open `Developer options` and enable `USB debugging`.

If `adb` fails with a `no devices/emulators found` error, it is possible that the device is connected to the computer in charging only mode. In this case, you should see a notification on the device to change this to debugging.

## Downloading a build from CI

We use Bitrise for continuous integration. It is configured to build a binary every time a commit is pushed to the repository. These binaries can be found by navigating [to this page](https://www.bitrise.io/app/db9b195f645cfed7#/builds_) and opening the details of any build. The details page will contain a generated APK called `app-debug.apk` under the "Apps & Artifacts" tab.

## Build and deploy Simple Server

Follow the [simple-server instructions](https://github.com/simpledotorg/simple-server/blob/master/README.md).

## Miscellaneous

#### Java 8's date and time

Simple uses [lazythreetenbp](https://github.com/gabrielittner/lazythreetenbp) for working with date and time. Due to some limitations, the IDE does not understand how to download its sources. As a work around, the sources can be downloaded from [the maven repository](http://search.maven.org/#search%7Cga%7C1%7Cthreetenbp) and manually attached to Android Studio.

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