# Checklist for adding or changing a build variant

## For adding a new build variant

- Add the product flavour in the `productFlavours` closure in `app/build.gradle`.
- Update the `variantFilter` closure to remove the unnecessary build for the new variant. 
  - For e.g.: we don't want the ability to create debug variants of flavors like Demo and Production.
- Add the API endpoint in `gradle.properties` and add this as the `API_ENDPOINT` build config field when defining the product flavour in `app/build.gradle`. This might need to be overriden on Bitrise based on the needs of the build flavour.
- Add the Heap ID in `gradle.properties` and add this as the `HEAP_ID` build config field when defining the product flavour in `app/build.gradle`. This might need to be overriden on the Bitrise based on the needs of the build flavour.
- Update the `applicationIdSuffix` and `versionNameSuffix` when defining the product flavour.
- Update the `afterEvaluate` closure at the end of the `android` block in `app/build.gradle` to include the build tasks for the new variant (only needed if you have enabled proguard for the build).
  - For e.g.: We only use Proguard to minify source files and not to obfuscate them. However, Proguard still creates empty `mapping.txt` files. We use the `afterEvaluate` block to delete those empty `mapping.txt` files.

## For both adding and renaming a build variant

- All resources must be updated at the path `app/src/<build flavour>/res`.
- The app name must be added at `values/strings.xml`, with the resource id `app_name`.
- Update the launcher icons for pre-sdk26 in the `mipmap-<density>` folders.
- Update the adaptive icon foreground for sdk26+ in `drawable-v26`.
- Add the `logo_large` drawable resource in the `drawable` folder. This is the large logo with text used on the app bar in some screens.
- Add the `ic_icons_logo` drawable resource in the `drawable` folder. This is the small logo (without text) used on the app bar in the home screen.
