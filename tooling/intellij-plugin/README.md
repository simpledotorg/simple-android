# Simple IntelliJ Plugin
This plugin houses tooling and capabilities that makes life easier while building the Simple Android app.

![Simple.org IntelliJ Plugin](doc/simple-intellij-plugin.png)

## What's in the package?
The plugin contains,
- A `uuid()` function expression to generate new UUID strings, commonly used in tests
- A collection of Live Templates that make working with UUIDs easier
- A collection of Live Templates that make working with timestamps easier

## Installation
1. Go to Preferences (`Cmd + ,`) or Android Studio → Preferences
2. Search for `Plugins` in the Preferences Dialog
3. Click on the `Cog Icon` and Choose `Install Plugin from Disk…`
4. Choose the latest release (`yyyy-mm-dd` format) from the `releases` directory

## Releases
To make changes and release a new version of the plugin, do the following.

1. Use IntelliJ Community / Ultimate Edition to open the project.
   - Click on `Create New Project`
   - From the `New Project` window,
     - Select `IntelliJ Platform Plugin` from the left pane
     - Then select `IntelliJ IDEA IU-xxx.xxxx.xx` from Project SDK on the right pane
     - Click on `Next`
   - In the next screen click on the `…` icon to select the `Project location`,
     - Point to `[simple-android]/tooling/intellij-plugin`
     - Click on `Finish`
     - If a `File Already Exists` dialog shows up, click on `Yes` to overwrite the `.iml` file
   - One the IDE opens the project, do a `git reset --hard` (this is hacky, but we'll update this once we find a solution)
2. Make the desired changes.
3. Open the `/resources/META-INF/plugin.xml` file and bump up the plugin version number specified inside the `<version>` tag. Please follow [SemVer](https://semver.org/) to update the version number.
4. Update `CHANGELOG.md` to mention the changes that were made.
5. Go to `Build → Prepare Plugin Module for Deployment`.
   - This will generate a `intellij-plugin.jar` in the project's root directory
   - Move this generated jar into the `releases` directory and rename the file to the following format - `yyyy-mm-dd.jar`
