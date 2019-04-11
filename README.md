[![Build Status](https://app.bitrise.io/app/db9b195f645cfed7/status.svg?token=0UVLxgCzsz75d21FUnkfhg&branch=master)](https://www.bitrise.io/app/db9b195f645cfed7)
[![pullreminders](https://pullreminders.com/badge.svg)](https://pullreminders.com?ref=badge)

# Simple

An Android app for recording blood pressure measurements.

## Building

1. Clone the project using git
2. Install [Android Studio](https://developer.android.com/studio/install#mac)
3. Import the project into Android Studio

When building for the first time, gradle will download all dependencies so it'll take a few minutes to complete. Subsequent builds will be faster.

### Building on Windows?

The project uses [Heap Analytics](https://heapanalytics.com/) which is integrated via a gradle plugin. The build plugin (currently) only works on macOS and Linux, so the project cannot be built on a Windows computer. To build on Windows, you will have to manually remove the Heap plugin -- and references to it -- from the source.

## Running the app

An APK can be built and installed to a device directly from Android Studio. The device could be an emulator or a real device. Emulators come pre-configured to run binaries, but running on a real device requires enabling `USB debugging` in phone settings. The steps for finding this setting varies with manufacturers, but should be somewhat along the lines of,

- If your phone settings has a search option, try searching for `Build number`.
- If search is unavailable, try navigating to `Settings > System > About phone`. Scroll to the bottom to find `Build number`.
- Tap on `Build number` for 5 times until you see a message saying "You are now a developer!".
- Go back to phone settings. A new setting group called  `Developer options `will now be available.
- Open `Developer options` and enable `USB debugging`.

If `adb` fails with a `no devices/emulators found` error, it is possible that the device is connected to the computer in charging only mode. In this case, you should see a notification on the device to change this to debugging.

## Build and deploy Simple Server

Follow the [simple-server instructions](https://github.com/simpledotorg/simple-server/blob/master/README.md).
