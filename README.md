[![Build Status](https://app.bitrise.io/app/db9b195f645cfed7/status.svg?token=0UVLxgCzsz75d21FUnkfhg&branch=master)](https://www.bitrise.io/app/db9b195f645cfed7)
[![pullreminders](https://pullreminders.com/badge.svg)](https://pullreminders.com?ref=badge)

# Simple

An Android app for recording blood pressure measurements.

## How to build

1. Clone the project using git
2. Install [Android Studio](https://developer.android.com/studio/)
3. Import the project into Android Studio

When building for the first time, gradle will download all dependencies so it'll take a few minutes to complete. Subsequent builds will be faster.

## Building on Windows?

The project uses [Heap Analytics](https://heap.io/) which is integrated via a gradle plugin. The build plugin (currently) only works on macOS and Linux, so the project cannot be built on a Windows computer. To build on Windows, you will have to manually remove the Heap plugin -- and references to it -- from the source.

## Build and deploy Simple Server

Simple Server is in a separate repository, and you should follow the [instructions there](https://github.com/simpledotorg/simple-server/blob/master/README.md).
