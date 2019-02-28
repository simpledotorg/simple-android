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

## Build and deploy Simple Server

Follow the [simple-server instructions](https://github.com/simpledotorg/simple-server/blob/master/README.md).

## Checklist for adding/renaming a build variant

#### Only for a new build variant
- Add the product flavour in the `productFlavours` closure in `app/build.gradle`.
- Update the `variantFilter` closure to remove the unnecessary build for the new variant.
- Add the api endpoint in `gradle.properties` and add this as the `API_ENDPOINT` build config field when defining the product flavour in `app/build.gradle`. This might need to be overriden on the CI based on the needs of the build flavour.
- Add the Heap ID in `gradle.properties` and add this as the `HEAP_ID` build config field when defining the product flavour in `app/build.gradle`. This might need to be overriden on the CI based on the needs of the build flavour.
- Update the `applicationIdSuffix` and `versionNameSuffix` when defining the product flavour.
- Update the `afterEvaluate` closure at the end of the `android` block in `app/build.gradle` to include the build tasks for the new variant (only needed if you have enabled proguard for the build).

#### For both adding and renaming a build variant
- All resources must be updated at the path `app/src/<build flavour>/res`.
- The app name must be added at `values/strings.xml`, with the resource id `app_name`.
- Update the launcher icons for pre-sdk26 in the `mipmap-<density>` folders.
- Update the adaptive icon foreground for sdk26+ in `drawable-v26`.
- Add the `logo_large` drawable resource in the `drawable` folder. This is the large logo with text used on the app bar in some screens.
- Add the `ic_icons_logo` drawable resource in the `drawable` folder. This is the small logo (without text) used on the app bar in the home screen.


