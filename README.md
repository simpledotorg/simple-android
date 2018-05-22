[![Build Status](https://www.bitrise.io/app/db9b195f645cfed7/status.svg?token=0UVLxgCzsz75d21FUnkfhg&branch=master)](https://www.bitrise.io/app/db9b195f645cfed7)

RedApp, a mobile app for recording blood pressure measurements.

RedApp uses a [library](https://github.com/gabrielittner/lazythreetenbp) to adapt ThreeTenBP (backport of Java 8's date and time packages). Due to some limitations, the IDE does not know hwo to download its sources. To work around, this project includes the sources as a separate file inside `libs` folder that can manually be attached to Android Studio.
