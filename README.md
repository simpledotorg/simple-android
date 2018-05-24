[![Build Status](https://www.bitrise.io/app/db9b195f645cfed7/status.svg?token=0UVLxgCzsz75d21FUnkfhg&branch=master)](https://www.bitrise.io/app/db9b195f645cfed7)

RedApp, a mobile app for recording blood pressure measurements.

This readme is currently a dump of useful information for building and working with this project. This will be organized in a better way soon.

#### Working with Java 8's date and time

RedApp uses a [library](https://github.com/gabrielittner/lazythreetenbp) to adapt ThreeTenBP (backport of Java 8's date and time packages). Due to some limitations, the IDE does not know how to download its sources. To work around, the sources can be downloaded from [the maven repository](http://search.maven.org/#search%7Cga%7C1%7Cthreetenbp) and manually be attached to Android Studio.

#### Debugging syncing of patient

[WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) is used for periodic scheduling syncing of patients. For debugging the state of the jobs, use this command:

```
adb shell dumpsys jobscheduler | grep org.resolvetosavelives.red
```

