# CHANGELOG

## Next Release

### Internal

- Bump datadog sdk to v1.15.0
- Bump Dagger to v2.44.2
- Bump Sentry Gradle Plugin to v3.3.0
- Bump leakcanary to v2.10
- Replace Tamil Nadu app logo and illustrations with DPH logo

## 2022-11-14-8505

### Internal

- Bump Jackson to v2.14.0
- Bump sentry to v.6.7.0
- Update patient lookup exception message when reporting
- Add illustrations for Tamil Nadu app
- Bump Kotlin to v1.7.21
- Fix Tamil Nadu `google-services.json` not present in correct folder
- Bump androidx-test-junit to v1.1.4
- Remove max SDK version for storage permissions
- Bump AndroidX test dependencies
- Show intro video screen based on registration config flag
- Remove Simple video from `PatientsTabScreen`

## 2022-11-07-8490

### Internal

- Add renovate package rule for Flipper updates
- Show all facilities in the state when changing assigned or appointment facility
- Add missing `uses-feature` in `AndroidManifest`
- Add color scheme for Tamil Nadu app
- Request notification permission on devices running Android 13
- Add a product flavor for TamilNadu app
- Bump google play services location to v21.0.1
- Bump Sentry Gradle Plugin to v3.2.1
- Move splash screen lottie config to XML
- Show notification permission rationale dialog when user doesn’t allow notification permission
- Update app icons and strings for Tamil Nadu app

## 2022-10-31-8480

### Internal

- Remove old custom prescription entry components
- Bump Material Design Components to v1.7.0
- Bump mixpanel to v7.2.0
- Bump auto request review to v0.8.0
- Bump Fragment to v1.5.4
- Run overdue appointment queries in a transaction
- Bump flipper to v0.171.1
- Bump Open CSV to v5.7.1
- Bump iText to v7.2.4
- Enable patient line list download for Indian users only
- Bump Sentry to v6.6.0

## 2022-10-26-8474

### Internal

- Update patient line list query
  - Filter out deleted patients
  - Use latest phone number that is not deleted
  - Use latest recorded BP
- Bump lint to v30.3.1
- Bump sentry gradle plugin to v3.2.0
- Bump google play services location to v21.0.0

### Fixes

- Fix app crashing when handling deep link results
- Fix diabetes warning dialog showing in patient screen for facilities with diabetes management disabled

## 2022-10-17-8463

### Internal

- Mark `diagnosedWithHypertension` and `diagnosedWithDiabetes` as nullable in `PatientLineListRow`
- Bump Sentry to v6.5.0
- Bump Sentry Gradle Plugin to v3.1.7
- Add background worker for downloading patient line list
- Bump Flipper to v0.169.0
- Use paginated data for generating patient line list CSV
- Enable large table support when converting CSV to PDF
- Move `bpCreatedAfter` and `bpCreatedBefore` date calculation to `PatientLineListCsvGenerator#generate`
- Add select line list format dialog
- Bump AGP to v7.3.1
- Bump asm to v9.4
- Fix patient line list not showing assigned facility name
- Bump firebase remote config to v21.2.0

### Features

- Add patient line list download option

## 2022-10-11-8452

### Internal

- Bump datadog sdk to v1.14.1
- Bump Kotlin to v1.7.20
- Add missing fields in `PatientLineListRow`
  - Added latest BP passport
  - Added registration and assigned facility IDs
- Implement patient line list CSV generator
- Add serial numbers in patient line list CSV
- Add patient line list downloader

## 2022-10-03-8443

### Internal

- Bump Lint to v30.3.0
- Bump Flipper to v0.164.0
- Rename `uiFlows` directory to `maestroUiFlows`
- Use the date at which the release branch is cut as the branch name
- Change concurrency group name for maestro tests workflow
- Bump Moshi to v1.14.0
- Add query to fetch patient line list data
- Bump core-ktx to v1.9.0

## 2022-09-26-8435

### Internal

- Bump AppCompat to v1.5.1
- Enable tracking automatic events from Mixpanel
- Sample events sent to Mixpanel
- Bump AGP to v7.3.0
- Remove `ConfigReader` injection from `ReleaseClinicApp`
- Add support for Android 13
  - Bump compile and target SDK to 33
- Bump google services to v4.3.14
- Bump Android desugar JDK library to v1.2.2
- Bump eclipse jgit to v6.3.0.202209071007-r
- Bump Sentry to v6.4.1
- Bump Sentry Gradle Plugin to v3.1.6
- Bump datadog sdk to v1.14.0
- Cut a release branch if current date is greater than or equal to expected release date
- Bump androidx-annotation to v1.5.0
- Fix incompatible types in androidx-annotation library update
- Add Maestro UI test for login flow
- Add github action to verify maestro tests
- Bump Fragment to v1.5.3

## 2022-09-16-8420

### Internal

- Add `patientId` and `facilityId` columns in `CallResult` table
- Add sync support for pulling overdue call results

## 2022-09-12-8414

### Internal

- Bump firebase remote config to v21.1.2
- Bump Google play services auth to v20.3.0
- Bump datadog gradle plugin to v1.5.1
- Bump Open CSV to v5.7.0
- Bump Jackson to v2.13.4
- Bump mixpanel to v7.0.1

### Fixes

- Fix overdue count in the app not matching with the count from the dashboard
- Fix appointment sheet showing after follow-up visit is added
- Fix ID search not working from scan screen

## 2022-08-30-8400

### Internal

- Bump mixpanel to v7.0.0
- Bump mlkit barcode scanning to v18.1.0
- Use heroku 20 on review apps
- Remove unnecessary dependencies from `ConfirmResetPinDialog`
- Bump flipper to v0.162.0
- Bump Sentry to v6.4.1
- Bump Sentry plugin to v3.1.5

### Fixes

- Fix hypertension treatment question showing Sri Lanka translation in India
- Fix spacing in the patient summary cards

## 2022-08-25-8383

### Fixes

- Fix app crashing when patient summary add phone number dialog is shown

## 2022-08-23-8378

### Fixes

- Fix app crashing when clicking on Overdue download or share buttons

## 2022-08-22-8375

### Internal

- Bump AppCompat to v1.5.0
- Migrate `LoggedOutOfDeviceDialog` to `BaseDialog`
- Bump flipper to v0.157.0
- Bump AGP to v7.2.2
- Initialise `ViewModel` in `BaseDialog#onCreate`
- Migrate `BloodSugarUnitSelectionDialog` to `BaseDialog`
- Migrate `ConfirmRemoveBloodSugarDialog` to `BaseDialog`
- Bump flipper to v0.159.0
- Bump Mobius to v1.5.9
- Bump Sentry to v6.4.0

## 2022-08-15-8368

### Internal

- Migrate `BloodSugarEntrySheet` to use `BaseBottomSheet`
- Migrate `UpdatePhoneNumberDialog` to `BaseDialog`
- Bump dagger to v2.43.2
- Bump gradle to v7.5.1
- Bump lint to v30.2.2
- Migrate `AddPhoneNumberDialog` to `BaseDialog`
- Fix download/share button showing in overdue search screen outside India
- Add support for Android 13
  - Bump compile and target SDK to 32
- Bump fragment to v1.5.2
- Bump threeten-extra to v1.7.1
- Fix app crashing when overdue search screen is opened

## 2022-08-08-8359

### Internal

- Bump fragment to v1.5.1
- Show chip input text view in overdue search when overdue v2 search is enabled
- Add overdue search query for supporting multiple search inputs
- Fix overdue search history and results not showing in overdue search screen
- Add multiple search inputs support in overdue search screen
- Remove overdue search v1 implementation
- Bump flipper to v0.156.0
- Bump jgit version
- Fix select all not updating checkbox state in the overdue search screen
- Bump Gradle to v7.5
- Run prescription refill queries in a transaction
- Use `PrescriptionRepository#refill` to refill prescriptions from edit medicines screen
- Use `PrescriptionRepository#refillForTeleconsulation` to refill prescriptions from teleconsult screen
- Migrate `BloodSugarHistoryScreen` to use view effects
- Trigger separate effects for loading and showing blood pressure history
- Migrate `BloodPressureHistoryScreenEffectHandler` to use view effect
- Migrate `ContactPatientBottomSheet` to use view effects
- Bump Sentry to v6.3.1
- Bump Mixpanel to v6.5.2
- Bump Sentry plugin to v3.1.4
- Add support for partial search input in overdue search screen

## 2022-08-01-8350

### Internal

- Point accordion downwards when overdue list is expanded
- Fix `ContactPatientBottomSheet` not opening completely
- Bump Sentry to v6.3.0
- Bump Sentry gradle plugin to v3.1.3
- Bump mixpanel to v6.5.1
- Add overdue search v2 feature flag
- Implement query to fetch village and patient names in the facility
- Add custom text field component with chip input
- Display search suggestions after loading in overdue search screen
- Load village and patient names when overdue search screen is created
- Don't show search history when overdue search v2 is enabled
- Bump room to 2.4.3
- Remove overdue search v2 feature flag key
- Fix overdue search not searching on query changes
- Bump dagger to v2.43.1

## 2022-07-25-8342

### Internal

- Update overdue patient card spacings
- Fix secure call button showing when securing calling is disabled for the facility
- Bump flipper to v0.154.0
- Add support for selecting all overdue appointment search results
- Add support for runtime permissions and network status in overdue search screen
- Implement `AppointmentRepository#searchOverduePatientsImmediate`
- Fix search results appointments not getting fully downloaded/shared/selected
- Remove unused code from overdue search
- Add overdue patients select and download feature flag key
- Fix overdue search not displaying no patients found error view

### Fixes

- Fix app crashing when editing patient details
- Fix hypertension treatment question english translation for Sri Lanka

## 2022-07-18-8333

### Internal

- Handle selected overdue appointments state in overdue screen
- Add support for selected overdue appointments download
- Show selected overdue appointments count in overdue screen
- Show download and share button in overdue search screen when search results are loaded
- Bump itext7 to 7.2.3
- Fix app crashing when scheduling overdue download worker
- Handle nullable fields when generating overdue CSV from database
- Add support for selected download and share in overdue screen
- Bump Sentry to v6.2.1
- Ignore deleted and old phone numbers when generating overdue CSV from database
- Remove extra margin start and end for overdue search button
- Show selected overdue appointments count in overdue search screen
- Implement `OverdueAppointmentSelector`
- Use overdue appointment selector in overdue screen
- Use overdue appointment selector in overdue search screen
- Use overdue appointment selector to fetch selected ids in overdue download worker
- Use overdue appointment selector to fetch selected ids in overdue download format selection dialog
- Remove unnecessary code in overdue screen
- Update call result outcome design in contact patient bottom sheet
- Add support for select and download in overdue search screen
- Add support for select and share in overdue search screen
- Show no internet connection dialog when download/share button is clicked
- Bump Material Design Components to v1.6.1
- Bump firebase config to v21.1.1
- Bump Coroutines to v1.6.4

### Changes

- Continue with current action when not now is clicked in measurement warning dialog

## 2022-07-11-8325

### Internal

- Bump Camera view dependency to v1.1.0
- Add query to fetch required information for generating overdue patients CSV
- Implement overdue patients CSV generator
- Refactor `OverdueListDownloader`
- Bump flipper to v0.153.0
- Set target aspect ration and rotation for QR code scanner preview
- Use `OverdueListDownloader` for generating overdue patients CSV or PDF using `OverdueCsvGenerator`
- Bump mixpanel to v6.4.0
- Bump fragment to v1.5.0
- Show a checkbox when select overdue patients feature flag is enable
- Handle selected overdue appointments state in overdue search screen
- Bump kotlin to v1.7.10
- Hide overdue patients list item checkbox by default

### Fixes

- Fix CDSS alert not dismissing after modifying medicines

## 2022-07-04-8318

### Internal

- Prefill search query when search history item is clicked
- Highlight search results with the matched search query
- Bump datadog sdk to v1.13.0
- Move existing search history item to the top when clicked
- Bump flipper to v0.150.0
- Bump sentry gradle plugin to v3.1.2
- Bump sentry to v6.1.4
- Bump Android desugar JDK library to v1.1.6
- Increase overdue search query text field debounce time
- Add remote key for overdue search feature flag
- Fix `JSONDataException` when parsing the drug stock response
- Ignore `ReportsModel#reportsContent` on parcel
- Bump Facebook.soloader to v0.10.4
- Bump CameraX dependencies to v1.1.0
- Bump resync token for appointment sync API
- Bump google-services to v4.3.13
- Use UI renderer to render overdue search results
- Add support for caching paging data

## 2022-06-27-8310

### Internal

- Fix old synced appointments not getting deleted on purge

## 2022-06-27-8307

### Internal

- Save search query to overdue search history when search query is valid
- Add layout for overdue search screen
- Bump Sentry to v6.1.2
- Consume `OverdueSearchViewEffect` in `OverdueSearchEffectHandler`
- Implement query to search overdue patients
- Open overdue search screen when search button is clicked in overdue screen
- Search for overdue patients when search query is valid
- Turn off translation for instant search allowed characters
- Updated translations: `om-ET`, `kn-IN`, `so-ET`, `ti-ET`, `mr-IN`, `te-IN`, `sid-ET`, `ta-IN`, `bn-IN`, `hi-IN`, `pa-IN`, `ta-LK`, `am-ET`,
  `bn-BD`, `si-LK`
- Show search results when overdue search results are loaded
- Handle user interactions for overdue search results
- Implement overdue search results adapter
- Handle overdue search load states
- Search overdue patients, when the search history item is clicked
- Prefill search query when the search history item is clicked
- Bump coroutines to v1.6.3
- Implement view effects to show overdue search results
- Wire Mobius loop with overdue search screen UI
- Handle keyboard visibility in overdue search screen
- Bump mixpanel to v6.3.0
- Bump Google Services plugin to v4.3.12
- Don't show no patients found error view when search query is empty
- Don't show empty search history items in overdue search screen

## 2022-06-20-8300

### Internal

- Fix inconsistent overdue section header UI
- Bump sentry-android to v6
- Bump sentry android gradle plugin to v3.1.0
- Bump Google Play Services Location to v20
- Bump Sentry Gradle plugin to v3.1.1
- Bump Kotlin to v1.7.0
- Hide footer when the pending section is collapsed
- Restore expanded/collapsed states of Overdue Sections
  - Use `OverdueListSectionStates` to update overdue section states in `OverdueModel`
  - Use `OverdueListSectionStates` to check overdue section states in `OverdueAppointmentListItemNew`
  - Remove unused section header state usages
- Add developer flag for overdue search
- Add search button in overdue patients list
- Implement FTS table for `PatientAddress`
- Define overdue appointment query as a `const` in `OverdueAppointment`
- Bump Annotation library to v1.4.0
- Provide overdue search history string set from preferences
- Add effect to load overdue search history
- Show overdue search history when search query is empty
- Load overdue search history when overdue search screen is opened
- Add overdue search query validator
- Validate search query when overdue search query is changed
- Bump overdue sections feature flag version to v1
- Save overdue search history as `String` type in preferences
- Implement `OverdueSearchHistory`
- Bump Sentry to v6.1.1

## 2022-06-13-8291

### Internal

- Add wrapper class for overdue appointment sections
- Show no pending patients card when there are no pending overdue patients
- Load appointments with a reminder when loading all overdue appointments for a facility
- Update horizontal padding of overdue section header
- Link overdue list adapter to the `RecyclerView` in `OverdueScreen`
- Implement `OverdueAppointmentListItemNew` adapter
- Rename `SeeAllPendingAppointmentList` to `PendingListFooter`
- Handle pending list footer clicks
- Fix app crashing after coming back to app from the background after the overdue list is loaded
- Handle see all/see less button clicks for pending overdue list section
- Fix views flashing in the overdue list
- When overdue appointments are loaded or are loading, render appropriate views
- Don't show pending overdue list footer if there are no pending overdue appointments
- Add pending list size for the default state as a firebase remote config
- Purge old appointments that are synced
- Implement `OverdueAppointmentListItemNew` adapter
- Fix progress page not handling system back clicks
- Bump Flipper to v0.149.0
- Bump OkHttp to v4.10.0
- Handle collapse/expand states of overdue appointment sections
- Fix no pending patients illustration is not collapsing

### Fixes

- Fix overdue list showing older scheduled appointments

## 2022-06-06-8284

### Internal

- Enable drug stock only for India
- Bump kotlin-coroutines to v1.6.2
- Move shared test code to separate module
- Remove overdue appointments list from `OverdueModel`
- Remove pull reminder status from `README.md`
- Change return type of `AppointmentRepository#overdueAppointmentsInFacilityNew` to `Observable<List<OverdueAppointment>>`
- Load overdue appointments and break down into different sections
- Bump Flipper to v0.147.1
- Show call result section by default in ContactPatientBottomSheet
- Bump lottie to v5.2.0
- Render overdue appointments when appointments are loaded and overdue sections feature is enabled
- Add feature flag for overdue section improvements
- Load overdue appointments when current facility is loaded and overdue sections feature is enabled
- Rename agreed to visit call result text
- Change colour of number and chevron in the section headers to blue
- Rename remind to call later call result text
- Show all overdue patients count instead of just pending patients in overdue tab
- Show progress bar when loading all overdue appointments
- Implement minor overdue section layout improvements
- Bump core-ktx to 1.8.0
- Fix `RetentionType` getting obfuscated by R8/Proguard
- Bump appcompat to v1.4.2
- Fetch the last created appointment for the patient when fetching the latest overdue appointment

### Changes

- Update english text of hypertension treatment question for Sri Lanka

## 2022-05-30-8273

### Internal

- Bump flipper to v0.146.0
- Fix test name in `PatientSummaryUpdateTest`
- Bump mixpanel to v6.2.2
- Bump datadog plugin to v1.4.0
- Show call result status for the appointment in `ContactPatientBottomSheet`
- Limit instant search to alphanumeric characters
- Add query to load all overdue appointments for a facility
- Bump lint to v30.2.1
- Add layouts for overdue appointments item types
- Fix lint issues related to overdue layouts

### Fixes

- Fix search crashing when searching with special characters

## 2022-05-23-8262

### Internal

- Bump Sentry to v5.7.4
- Fix CDSS alert hiding when multiple BPs are added on the same day
- Bump flipper to v0.145.0
- Don't show CDSS alert for newly registered patients
- Add query to load latest scheduled appointment for the patient
- Don't show CDSS alert for patient if appointment is more than 2 days overdue
- Bump ConstraintLayout to v2.1.4

### Changes

- Show ongoing hypertension treatment question for Sri Lanka
- Redesign `ContactPatientBottomSheet` UI

### Fixes

- Fix text cursor jumping to end when editing patient details

## 2022-05-16-8253

### Internal

- Enable CDSS alerts only in pilot facilities
- Redact measurements PHI from logs
- Bump lint to v30.2.0
- Bump Cirrus rebase-action to v1.7
- Bump firebase-config to v21.1.0
- Add query to fetch call result for the appointment
- Bump Jackson to v2.13.3

## 2022-05-09-8244

### Internal

- Add `updatePriorities` for security app update reminders as a Firebase remote config
- Add `appStaleness` in `ShowAppUpdate`
- Disable monthly drug stock reminder by default
- Show critical update card if in case it clashes with drug stock reminder card
- Bump Google play services auth to v20.2.0
- Fix critical app update dialog UI
- Bump Mixpanel to v6.2.1

### Fixes

- Fix app update notifications not being scheduled

## 2022-05-02-8233

### Internal

- Bump kotlin to v1.6.21
- Bump Lottie to v5.1.1
- Migrate `DeletePatientEffectHandler` to use view effects
- Update `DrugStockReminderAPI` to include `DrugStockFormURL`
- Handle opening of webView when enter drug stock button clicked
- Bump Datadog SDK to v1.12.0
- Bump leakcanary to v2.9.1
- Bump facebook flipper to v0.144.0
- Enable monthly drug stock reminder feature flag

### Features

- Add support for monthly drug stock report reminders

## 2022-04-25-8224

### Internal

- Bump Sentry to v5.7.2
- Bump Mixpanel to v6.1.1
- Bump gradle to v7.1.3
- Update translations: `am-ET`, `ti-ET`, `om-ET`, `bn-BD`, `hi-IN`, `so-ET`, `si-LK`, `te-IN`, `ta-IN`, `mr-IN`, `ta-LK`, `pa-IN`, `kn-IN`,
  `sid-ET`, `bn-IN`
- Increase max character limit in overdue tab badge
- Use personal access token when checking out repo in `cut_release_branch` workflow
- Sync reports and progress data before patient sync
- Replace `age` and `dateOfBirth` in `TestData` factory methods with `PatientAgeDetails`
- Bump Lint to v30.1.3
- Bump Cirrus rebase-action to v1.6
- Remove the `Age` class
- Add preferences for drug stock reminder
- Fix the logic for notification schedule time in `AppUpdateNotificationWorker`
- Fix API link for `DrugStockReminderApi#drugStockReminder`
- Check drug stock report status when app is opened
- Schedule `DrugStock` API to run on 1st of every month at 8 AM
- Migrate `ForgotPinCreateNewEffectHandler` to view effects
- Add feature flag for monthly drug stock report reminders
- Add `DrugStockCard` in `PatientsTabScreen` and handle its visibility
- Cut release branch every week instead of every 2 weeks
- Trigger demo and prod release when release branch is created
- Schedule `DrugStockNotifcationScheduler` in the `PatientsTabScreen`
- Migrate `ForgotPinConfirmPinEffectHandler` to use view effects
- Bump Flipper to v0.143.0
- Rename `trigger_demo_release` workflow name to `trigger_release`

## 2022-04-18-8216

### Internal

- Bump GH Actions `cache` to v3
- Fix `trigger_demo_release` workflow running on every newly created branch
- Implement critical app update card in `PatientsTabScreen`
- Bump flipper to v0.140.0
- Use `RecyclerView.AdapterDataObserver` to scroll to top when drugs are added
- Update AVD disk size to 8GB in GH Actions
- Bump sentry-android to 5.7.1
- Bump sentry gradle plugin to 3.0.1
- Implement showing app update nudges based on the priority in `PatientTabScreen`
- Remove marking older appointments for a patient as a side effect when scheduling an appointment
- Bump datadog to v1.3.0
- Update flipper to v0.141.0
- Add a query to check whether the latest BP entry is high for the patient
- Load clinical decision support info when the patient summary screen is created
- Bump gradle to v7.4.2
- Bump kotlin to v1.6.20
- Bump coroutines to v1.6.0
- Implemented the support for DrugStockReminder API
- Show the clinical decision support banner in summary screen when newest BP entry is high for the patient
- Hide clinical decision support alert banner when edit medicines are clicked
- Bump asm to v9.3
- Add feature flag for clinical decision support alerts
- Update `isNewestBpEntryHigh` query
- Add Hindi translations for CDS alert
- Add remote config key for `NotifyAppUpdateAvailableV2` feature flag
- Add GitHub action to cut a release branch

### Changes

- Add stronger app update reminders

### Fixes

- Fix recycler view crash when refilling drugs

## 2022-04-04-8192

### Internal

- Bump fragment to v1.4.1
- Bump firebase to v21.0.2
- Bump flipper to v0.138.0
- Bump sentry to v5.6.3
- Bump paging to v3.1.1
- Handle background notifications for prioritised app updates
- Bump Sentry Gradle plugin to v3.0.0
- Add `DrugStockReminder` api support
- Bump Sentry to v5.7.0
- Add GH Action to trigger a demo release on Bitrise when release branch is created
- Run CI checks in `trigger_demo_release` workflow
- Cancel in progress demo release workflow when new release action is triggered
- Pass Simple Bitrise app slug to `TriggerBitriseRelease` script
- Add `CriticalAppUpdateDialog`

## 2022-03-21-8173

### Internal

- Bump AGP to 7.1.2
- Bump lint to v30.1.2
- Bump Lottie to v5.0.2
- Use full text search when searching for patients
- Bump flipper to v0.137.0
- Bump GH Actions `upload-artifact` to v3
- Bump logback to v1.2.11
- Move wildcard formatting to search query
- Update GH Actions `checkout` to v3
- Bump Lottie to v5.0.3
- Bump Jackson to v2.13.2
- Bump mixpanel-android to 6.1.0
- Bump Sentry to v5.6.2
- Bump Gradle to v7.4.1
- Display patient search results in alphabetical order

### Changes

- Improve patient search performance
- Improve loading recent patients performance
- Improve loading overdue patients performance
- Show patient died status for dead patients in search results

## 2022-03-07-8157

### Internal

- Migrate `HelpScreen` to user view effects
- Bump Sentry Gradle plugin to v2.1.5
- Update translations: `am-ET`, `ta-LK`, `kn-IN`, `bn-BD`, `bn-IN`. `pa-IN`, `mr-IN`, `ta-IN`, `si-LK`, `sid-ET`, `te-IN`, `so-ET`, `ti-ET`, `om-ET`
  , `ta-LK`
- Bump sentry to 5.6.1
- Provide `AppUpdateHelpContact` based on the `Country`
- Bump flipper to v0.136.0
- Remove `isFlexibleUpdateType` as parameter from `UpdateInfo`
- Provide `PackageInfo` as dependency for `AppVersionFetcher`
- Bump Lottie to v5.0.1
- Update proguard rule to keep enum classes only for iText library
- Add support for prioritised nudges in `CheckAppUpdateAvailability`
- Use `OnConflictStrategy.ABORT` when updating using upsert
- Bump opencsv to v5.6
- Update Room to v2.4.2
- Update GH Actions `setup-jdk` to v3

## 2022-02-21-8141

### Internal

- Remove `InputField#validate`
- Migrate `RegistrationFullNameScreen` to use view effects
- Migrate `RegistrationLoadingScreen` to use view effects
- Bump Flipper to v0.131.1
- Migrate `RegistrationLocationPermissionScreen` to use view effects
- Bump Lottie to v4.2.2
- Bump ConstraintLayout to v2.1.3
- Bump Preferences to v1.2.0
- Migrate `RegistrationConfirmPinScreen` to use view effects
- Migrate `OnboardingScreen` to use view effects
- Bump desugar JDK library to v1.1.5
- Migrate `RecentPatientsScreen` to use view effects
- Fix `OnboardingScreen` not opening registration screen
- Mark `Appointment#nextAppointmentPatientProfile` query as a transaction
- Remove Gradle Versions plugin
- Migrate `RegistrationPinScreen` to use view effects
- Migrate `RegistrationFacilitySelectionScreen` to use view effects
- Bump Lint to v30.1.1
- Stop sending user location information to Mixpanel
- Bump Jackson to v2.13.1
- Bump Room to v2.4.1
- Migrate `LoginPinScreen` to use view effects
- Bump AGP to v7.1.1
- Stop sending username to analytics
- Stop reporting SQL performance metrics to Mixpanel
- Ignore updating to Material 3 dependency
- Bump Kotlin to v1.6.10
- Bump Moshi to v1.13.0
- Bump ItemAnimators to v1.1.0
- Bump Mixpanel to v6.0.0
- Migrate `AppLockScreen` to use view effects
- Bump Gradle to v7.4
- Bump RootBeer to 0.1.0
- Bump Google Play Services
  - Auth to v20.1.0
  - Location to v19.0.1
  - Barcode Scanning to v18.0.0
- Bump leakcanary to 2.8.1
- Bump coroutines to v1.6.0
- Remove Google Guava dependency
- Bump Dagger to v2.41

## 2022-02-07-8126

### Internal

- Bump Gradle to v7.3.3
- Remove version range for zxing dependency in the version catalog
- Bump WorkManager to v2.7.1
- Bump Google Play Services
  - MLKit Barcode Scanning to v16.2.1
  - Authentication to v19.2.0
- Update renovate bot config
- Bump OkHttp to v4.9.3
- Ignore automatic appointments when fetching next appointment details
- Bump AGP to v7.0.4
- Bump Play Core to v1.10.3
- Bump androidx-cameraView to 1.0.0-alpha32
- Bump Github action script to v5.1.0
- Bump Lint to v30.0.4
- Bump Dagger to v2.40.5
- Bump facebook.soloader to 0.10.3
- Bump Mobius to v1.5.8
- Request camera permissions when add bp passport button is clicked in `EditPatientScreen`
- Run renovate bot daily after 12 am
- Update next appointment card UI specifications
- Set toolbar title in `ScanSimpleIdScreen` based on where it's opened from
- Implement adding `NHID` in `EditPatientScreen`
- Request camera permission when add nhid button is clicked in `EditPatientScreen`
- Fix when scanned QR code error is shown the progress state continues to show
- Refactor `addBPPassport` and `addNHIDButton` to use `MaterialButton`
- Fix two duplicate NHIDs end up rendering in `EditPatientScreen`
- Bump firebase config to v21.0.1
- Add facility protocol and group UUID in demo facility
- Update assigned facility card UI specifications
- Set India timezone for renovate bot to run schedules
- Move adding health ids from edit patient behind a feature flag
- Display National Health ID before BP Passports in `EditPatientScreen`
- Bump google-services to 4.3.10
- Add highlight when a new alternate id is added
- Bump Google Truth to v1.1.3
- Bump Mixpanel to v5.9.6
- Bump androidx-annotation
- Add benchmark test for loading overdue appointments in a facility
- Support scanning list of unique Bp passports in `EditPatientScreen`
- Add benchmark test for patient registration queries
- Handle visibility of Bp Passport label and button when scan health id from edit patient is enabled
- Migrate `ScannedQrCodeSheet` to use view effects
- Bump lint to v30.1.0
- Bump Flipper to v0.131.0
- Add benchmark test for creating a new bp entry
- Migrate `CustomDrugEntrySheet` to user view effects
- Migrate `SettingsScreen` to use view effects
- Bump Paging to v3.1.0
- Migrate `RemoveOverdueAppointmentScreen` to user view effects
- Remove Spotless Gradle plugin
- Remove code formatting checks GH Action
- Bump AppCompat to v1.4.1
- Update the highlight alignment for newly added NHID or BP passport
- Add highlight when newly scanned bp passports are added
- Index `patientUuid` in tables that are joined in `OverdueAppointment` database view
- Fix broken back button in `EditPatientScreen`
- Fix uneven spacing between health ID labels and `ADD` button

### Changes

- Add health ID's from edit patient screen

## 2022-02-01-8118

### Internal

- Don't run auto request review action in draft PRs
- Implement `NextAppointmentCardView`
- Show next appointment card in patient summary screen
- Refresh appointment details when appointment is scheduled or assigned facility is changed
- Fix appointment not refreshing after assigned facility is changed
- Add double non breaking spaces between appointment date and status
- Debounce overdue updates to prevent running out of view effect queue size
- Stop loading total facility count when facility picker view is created
- Update `hasAppointmentForPatientChangedSince` query to only consider `Scheduled` appointments
- Update GH Actions `setup-jdk` to v2
- Bump Datadog SDK version
- Rename next appointment title string
- Add support for demo country

### Changes

- View next appointment information in patient summary screen
- Change appointment bottom sheet display logic
  - Schedule appointment sheet is now only opened from patient summary screen when BP or Blood Sugar or Prescription is changed

## 2022-01-11-8098

### Internal

- Inject a new `AppDatabase` in instrumented tests instead of using a singleton instance
- Add support for writing performance regression test suites
- Remove QA Android tests from PR comment GH Action
- Return early if the required `Timeout` conditions are not satisfied in `TimeoutCallAdapterFactory`
- Enable auto merge of PR when assigning reviewer

### Fixes

- Fix help screen not opening on button click

## 2021-12-28-8079

### Internal

- Remove duplicate included modules in `AppModule`
- Use hardcoded user in server integration tests
- Add support to set custom animations for `Router` transactions
- Migrate `DeletePatientScreen` to a fragment
- Migrate `TeleconsultPrescriptionScreen` to a fragment
- Report `StringIndexOutOfBoundsException` when building identifier display value
- Migrate `HelpScreen` to a Fragment
- Use `StringPreferenceConverter` for getting selected state from preferences
- Use view render to setup UI and fill fields in edit screen
- Remove unused properties from `SetupActivityModel`
- Fix text change events not triggering in edit patient screen
- Add support for running Heroku cleanup instance manually
- Migrate `EditMedicineScreen` to use view effects
- Migrate `OverdueScreen` to use view effects
- Add a specific endpoint network timeout

### Fixes

- Fix app crashing when changing facility while app is not connected to network

## 2021-12-08-8060

### Fixes

- Fix Sri Lanka personal health number not rendering in the patient entry and edit screens

## 2021-12-06-8056

### Internal

- Ask storage permission when download/share button is clicked
- Add common download function for downloading overdue list
- Fix flaky paging test cases
- Remove post delayed callbacks when view is detached from window
- Remove canceling previously scheduled periodic work in `SyncScheduler`
- Change SQL performance reporting to use begin and end hooks
- Use view binding to inflate layout in `BaseDialog`
- Fix overdue list file name in `OverdueListDownloader`
- Send performance monitoring events to Datadog
- Bump Mobius to v1.5.6
- Enable overdue list download and share feature in India only
- Extend `RxWorker` in `OverdueDownloadWorker`
- Implement overdue list download/share format dialog
- Set user ID as user property in `MixpanelAnalyticsReporter`
- Provide a standardized mechanism to update user and deployment details in third-party tooling
- Improve Room annotation processing
  - Enable star projection of queries
  - Enable incremental annotation processing
- Use `IO` scheduler for doing background tasks in `OverdueDownloadWorker`
- Download/Share CSV only for API below 24
- Show error dialog when overdue list download fails
- Fix sharing downloaded overdue list through Whatsapp is not working
- Enable `OverdueListDownloadAndShare` feature flag
- Make PDF as the default for downloading Overdue lists
- Default to opening Google Sheets for Spreadsheets

### Changes

- Add option to download & share overdue list

### Fixes

- Fix medical history answers toggle not switching when selected

## 2021-11-24-8041

### Internal

- Bump Mixpanel to v5.9.5
- Set user ID as user property in `MixpanelAnalyticsReporter`

## 2021-11-23-8038

### Internal

- Bump Dagger to v2.40
- Bump WorkManager to v2.7.0
- Migrate `SplashScreen` to a fragment
- Bump Core-KTX to v1.7.0
- Migrate `InstantSearchScreen` to use view effects
- Migrate `LinkIdWithPatientEffectHandler` to use view effects
- Migrate `PatientSummaryScreen` to use view effects
- Migrate `OnBoardingScreen` to a Fragment
- Migrate `SettingsScreen` to a Fragment
- Bump Coroutines to v1.5.2
- Implement `OverdueListDownloader`
- Cancel in progress GH actions when new action is started with same group name
- Migrate `HomeScreen` to use view effects
- Migrate `TeleConsultSuccessScreen` to a Fragment
- Migrate `ForgotPinCreateNewPinScreen` to a Fragment
- Create demo user for Google Play reviews
- Migrate `PatientEntryScreen` to use view effects
- Bump Gradle to 7.3
- Implement `OverdueDownloadWorker`
- Show no internet connection dialog when download/share button is clicked
- Save databases in failed instrumented tests as test artifacts
- Migrate `IntroVideoScreen` to use view effects

### Fixes

- Fix diabetes treatment input required dialog showing when diabetes management is disabled

## 2021-11-02-8005

### Internal

- Allow only latin digits in the phone number entry
- Bump AGP to v7.0.3
- Migrate `RegistrationFacilitySelectionScreen` to a fragment
- Migrate `RegistrationLocationPermissionScreen` to a fragment
- Migrate `RegistrationLoadingScreen` to a fragment
- Remove `FragmentScreenKeyModule` from `AuthenticationActivityComponent`
- Remove `:router` package
- Remove dependency on `com.squareup.flow:flow`
- Migrate `SelectCountryScreen` to use view effects
- Migrate `EnterOtpScreen` to use view effects
- Implement CSV to PDF file converter
- Add `OverdueListDownloadApi`
- Migrate `ForgotPinConfirmPinScreen` to a Fragment
- Remove tool for capturing of flaky tests

### Fixes

- Fix overdue screen progress state during initial sync

## 2021-10-18-7990

### Internal

- Migrate `SelectCountryScreen` to a fragment
- Bump Mobius to v1.5.5
- Enable custom drug search feature
- Enable placeholders in overdue screen
- Use overdue list count to display the count in the tab bar
- Change `TheActivity` to load the entire screen history instead of a single screen key
- Add support for Android 12
  - Bump compile & target SDK to 31
  - Add exported attr in `AndroidManifest.xml` for activities/services/receivers with intent filters
  - Add `ACCESS_COARSE_LOCATION` permission
- Fetch appointment directly in contact patient sheet
- Updated translations: `so-ET`, `pa-IN`, `bn-BD`, `mr-IN`, `bn-IN`, `ta-LK`, `am-ET`, `sid-ET`, `kn-IN`, `om-ET`, `si-LK`, `ta-IN`, `te-IN`
- Bump CameraX dependencies
- Remove unused params from `OverdueAppointment`
- Bump Dagger to v2.39.1
- Enable call result sync
- Scope the construction of `RxSharedPreferences` to the application
- Remove flow for setting a fallback country for India users when moving to a build supporting country selection
- Move migration of v1 Country to `SetupActivity` from the DI provider

### Changes

- Purge call results when database purges run
- Scroll to top when drugs list content is changed in edit medicines screen
- Render custom drug frequency in drug summary view
- Change "Normal" button text to "Call" in patient contact sheet if secure call is disabled

### Fixes

- Fix country selection list content hidden behind Android navigation bar
- Fix screen backstack not getting maintained when app is restored from background

## 2021-10-05-7973

### Fixes

- Fix state selection list content hidden behind Android navigation bar

## 2021-10-04-7971

### Internal

- Add a progress state in `CustomDrugEntrySheet`
- Implement showing drug frequency in `EditMedicineScreen` based on the country
- Add integration tests for `AppConfigRepository`
- Change `SynceableRepository#save()` to be a synchronous call
- Change server environment in integration tests to `android_review`
- Update `verify_pr_description` CI validation check to Shortcut links
- Unify appointment cancellation flow in `RemoveOverdueScreen`
- Implement showing medicine frequency labels depending on the country in `TeleconsultMedicinesView` and `MedicineFrequencySheet`
- Add support for syncing diabetes treatment question
- Use Kotlin script for PR comment check GH Action
- Bump Kotlin to v1.5.31
- Remove overdue list changes feature flag
- Implement providing drug frequencies label depending on the country in `DrugSearchScreen`
- Add progress state in the save button when the drug is being added/updated in `CustomDrugEntrySheet`
- Handle `ShowKeyboard` as a Ui Action instead of rendering it in the `UiRenderer`
- Refactor `ContactPatientUiRenderer`
- Provide drug frequencies to label as a map
- Show ongoing diabetes treatment required dialog in diagnosis screen
- Record call results instead of updating the same appointment record
- Fix `Country` v2 migration not running
- Convert commit and push bash script to Kotlin script
- Show state selection screen after country is selected
- Add Sri Lanka personal health number business identifier
- Enable custom drug search feature
- Inject `DrugFrequencyToLabelMap` directly in `TeleconsultMedicinesView` instead of handling it in the effect handler

### Changes

- Hide resend sms button when OTP attempts are blocked
- Show patient died status in patient summary screen & contact patient bottom sheet
- Go back to the previous screen when done/back is clicked in the patient summary screen when the patient is marked as dead
- Show change diagnosis dialog when patient is not diagnosed with HTN or diabetes when registering
- UI improvements for medical history screen
  - Show hypertension diagnosis and treatment in single card
  - Show separate cards for hypertension and diabetes diagnosis
  - Show hypertension diagnosis even when facility doesn't have diabetes management enabled
  - Added diabetes treatment question in medical history screen
- Medication screen improvements
  - Search for commonly used drugs
  - New custom drug entry/edit sheet
- Add support for Sri Lanka
- Add support for displaying drug frequencies label depending on the country
- Restrict OTP entries to 5 attempts
- Remove next button from state selection screen, you can now select a state to go to next screen
- Remove next button from the country selection screen, you can now select a country to go to next screen
- Custom Drug Entry Sheet UI Improvements
- Navigate back to `SelectCountryScreen` from `RegistrationPhoneScreen` when there's only one state present in the country
- Show personal health number text field in patient entry/edit screens

### Fixes

- Fix select country & state screen overlapping next button
- Fix contact patient bottom sheet not showing correct results
- Fix multiple medical history answers selection in patient summary screen

## 2021-09-23-7958

### Fixes

- Fix app not displaying national ID and proper illustration in Sri Lanka

## 2021-09-20-7952

### Internal

- Change `ScheduleAppointmentSheet` to use Mobius view effects
- Add Clubhouse overview description in the Pivotal documentation
- Migrate v1 `Country` usages to v2 `Country`
- Change `NewMedicalHistoryScreen` to use Mobius view effects
- Parse `Country_Old` manually when migrating to v2 `Country`
- Remove `Country_Old`
- Fetch states for selected country deployments
- Remove login failed errors from UiRenderer, and add via effect handler when logging in
- Replace deprecated platform `PreferenceManager` usage with AndroidX preference
- Bump Dagger to v2.38.1
- Bump AGP to v7.0.2
- Refactor logic around providing drug frequencies label depending on the country
- Change `EditPatientScreen` to use Mobius view effects
- Update `.editorconfig` rules
- Show facilities from selected state during sign up
- Use `Object` class for parsing old `Country` json

### Features

- Add support for state selection after selecting country

### Changes

- Implement providing drug frequencies label depending on the country
- Ui improvements for `CustomDrugEntrySheet`
  - Show numeric keyboard with dosage text field focused when sheet is opened
  - Update drug frequency edit text ui
- Add ellipsis to long drug name in `PatientSummaryScreen` & `EditMedicineScreen`
- Add a unicode character instead of space in `DrugSearchScreen` search results
- Updated translations: `mr-IN`, `bn-IN`, `bn-BD`, `kn-IN`, `pa-IN`, `am-ET`, `sid-ET`, `ta-LK`, `hi-IN`, `ti-ET`, `so-ET`, `si-LK`, `te-IN`, `ta-IN`
  , `om-ET`
- Show alpha numeric keyboard for national ID text field in patient entry and edit screen
- Remove maximum character limit for phone numbers

### Fixes

- Fix patient summary going back to home screen when opening teleconsult links

## 2021-09-08-7939

### Internal

- Bump Paging to v3.0.1
- Bump CameraX dependencies
  - Bump `camera-core`, `camera-camera2` and `camera-lifecycle` to v1.0.1
  - Bump `camera-view` to v1.0.0-alpha27
- Bump AGP to v7.0.1
- Bump Timber to v5.0.1
- Migrate `PlaceholderScreen` to a fragment
- Update `MaterialAlertDialog` theme and style
- Run integration tests on discrete Heroku servers instead of QA
- Bump lottie to v4.1.0
- Bump logback to v1.2.5
- Bump lint to v30.0.1
- Change large component corner shape to 8dp
- Add 32dp horizontal margin for `noPatientsInFacilityTextView` in `screen_instant_search`
- Migrate `EnterOtpScreen` to a Fragment
- Add v2 manifest support in app
- Fix TDS drug frequency string value
- Remove unused `DrugFrequencyChoiceItem` class

### Changes

- Implement `CustomDrugEntrySheet`

## 2021-08-23-7922

### Internal

- Set Simple video and duration based on locale
- Fix incorrect test in `TheActivityControllerTest`
- Commit and push string formatting changes
- Add `.gitattributes` for specifying eol for `strings.xml`
- Support `ContactPatientSheet` with no appointment
- Migrate to Gradle Version Catalog
- Move logic for deciding the initial screen to the Mobius loop in `TheActivity`
- Update OverdueListChanges feature flag to support `ContactPatientSheet` changes
- Change registered facility as nullable in `ContactPatientProfile`
- Add home screen illustrations for Sri Lanka
- Change Sri Lanka iso country code to `LK`
- Bump asm to v9.2
- Set Sri Lanka display name in country selection screen
- Migrate to Fragment results API
- Bump threeten-extra to v1.7.0
- Bump AppCompat to v1.3.1
- Bump RecyclerView to v1.2.1
- Unify the patient age details into a single model
- Migrate `PatientEntryScreen` to a fragment
- Check if ViewModel is initialised when saving state in base screens
- Bump Kotlin Coroutines to v1.5.1
- Bump jBCrypt to v0.4
- Migrate `EditPatientScreen` to a fragment
- Migrate `IntroVideoScreen` to a Fragment
- Remove usage of `Age` in `EditPatientEffectHandler`
- Move delete patient button click listener to `onViewCreated` in `EditPatientScreen`
- Prefill Sri Lanka national ID in patient entry and edit screens
- Add `SriLankaNationalIdMetaDataV1` to type adapter
- Bump RxJava to v2.2.21
- Bump LeakCanary to v2.7
- Remove reporting scanning logs to Sentry
- Migrate `NewMedicalHistoryScreen` to a Fragment
- Change `RegistrationPhoneScreen` to use view effects
- Change `ScanSimpleIdScreen` to use Mobius view effects
- Handle exception when syncing reports in `ConfirmFacilityChangeEffectHandler`
- Show appointment results in contact patient sheet for patient with no phone number
- Change `PatientsTabScreen` to use Mobius view effects

### Changes

- Ask users for their name and job designation on sign up
- Don't load search results if search query is not changed from previous search query
- Show keyboard and prefill search query if present when instant search screen is shown
- Updated translations: `kn-IN`, `bn-IN`, `hi-IN`, `ta-LK`, `pa-IN`, `om-ET`, `bn-BD`, `so-ET`, `mr-IN`, `ta-IN`, `si-LK`, `bn-BD`, `te-IN`, `am-ET`
  , `ti-ET`, `sid-ET`
- Show warning dialogs for adding BP and Blood Sugar after creating a patient
- Overdue list improvements
  - Change overdue list UI
  - Load overdue patients without phone number
  - Updated contact patient bottom sheet UI

### Fixes

- Fix app syncing prescription drugs with empty name
- Fix text overflowing in video illustration in some languages
- Fix searching by short code from the scan BP Passport screen does not load results initially

## 2021-08-09-7904

### Internal

- Add gradle-versions-plugin for checking dependency updates
- Bump AndroidX Core Testing to v2.1.0
- Bump Firebase dependencies
  - Performance monitoring plugin -> 1.4.0
  - Performance monitoring -> 20.0.2
  - Remote config -> 21.0.0
- Disable Firebase Performance Monitoring for development builds
- Use full date formatter when rendering last visited date in contact patient sheet
- Bump CameraX dependencies
  - Bump `camera-core`, `camera-camera2` to v1.0.0
  - Bump `camera-lifecycle` to v1.0.0
  - Bump `camera-view` to v1.0.0-alpha26
- Fix UI inconsistencies in overdue list and contact patient sheet
- Bump AppCompat to v1.3.0
- Cache build dependencies and intermediates in CI workflows
- Bump Fragment to v1.3.6
- Disable new `FragmentStateManager`
- Remove the daily and frequent sync separation
- Update home screen illustrations for Bangladesh & Ethiopia
- Update Simple video view to be translatable
- Bump Logback to v1.2.4
- Bump Core KTX to v1.6.0
- Stop triggering syncing of protocols on user login
- Bump AndroidX Test dependencies
  - Bump AndroidX Test to v1.4.0
  - Bump AndroidX Test Ext to v1.1.3
- Bump Rx preferences to v2.0.1
- Bump Lottie to v3.7.2
- Bump Lint to v27.2.2.
- Support compression of HTTP request bodies using Gzip
- Use `MobiusLoopViewModel` for sending view effects that are lifecycle aware
  - Replace `MobiusLoop.Controller` with `MobiusLoopViewModel` in base screens
  - Add new interface for handling received view effects
  - Remove `RxMobiusBridge`
- Fix navigation issues in `Router`
  - Support multiple modals
  - Fix crash when using new `FragmentStateManager`
- Migrate `ChangeLanguageScreen` to a fragment
- Bump Flipper to v0.98.0
- Bump UUID generator to v4.0.1
- Bump Jackson to v2.12.4
- Restart `TheActivity` after changing language
- Don't push to new screen if the top screen is same as the new screen
- Bump ML Kit Barcode to v16.2.0
- Bump Lint to v30.0.0
- Bump AGP to v7.0.0
- Bump Play Services Location to v18.0.0

### Changes

- Redesign sync indicator view
- Increase the search and scan button height in patients screen
- Show new video illustration in patients screen
- After language is changed app will go back to home screen

### Fixes

- Fix `ContactPatientBottomSheet` UI spacing and styling

## 2021-08-03-7898

### Fixes

- Fix showing duplicate patients in search results

## 2021-07-26-7883

### Internal

- Bump Sentry Gradle plugin to v2.0.1
- Bump Material Design Components to v1.4.0
- Add ADRs for SQL performance profiling
- Bump sqlite-android version to 3.36.0
- Add `minWidth` for phone number text input layout in `screen_registration_phone`
- Add `Drug`s table
- Migrate `BloodPressureHistoryScreen` to a fragment
- Migrate `BloodSugarHistoryScreen` to a fragment
- Delete patients and it's complete medical records when the retention time has passed
- Implement `DrugSync`
- Update default protocol drugs
- Remove remote config sync worker from the regular sync resources
- Enqueue `UpdateRemoteConfigWorker` on app cold starts
- Use `SyncConfigType` qualifier for daily and frequent sync configs
- Bump Kotlin to v1.5.21
- Updated `ContactPatientBottomSheet` behaviour for supporting patients with and without phone number
- Change `SyncInterval` from an enum to a data class
- Timeout instrumented tests after 30 minutes
- Change Android emulator API level to 27 in `ci_checks`
- Fix home screen memory leaks
  - Use `childFragmentManager` & `viewLifecycleOwner.lifecycle` when creating `FragmentStateAdapter`
  - Remove overdue list adapter from recycler view when `OverdueScreen` view is about to be destroyed
- Implement transferred from and registered at facility in `ContactPatientBottomSheet`
- Load online lookup API if patient not found locally
- Implement `DrugsSearchScreen`
- Don't show progress if overdue list already has items
- Change `PatientProfile` to be a Room relation model
- Move online patient lookup behind a feature flag

### Features

- Add support for finding a patient online from ID scan within the states

### Changes

- New supported language: Tamil (Sri Lanka) [`ta-LK`], Sinhala [`si-LK`]
- Show "Change" button in BP and blood sugar entry sheets for changing date
- Move all daily syncs to the frequent sync group

### Fixes

- Fix `ContactPatientBottomSheet` not going back to call patient view on back click in call later mode
- Fix overdue list not changing when switching facility from overdue screen

## 2021-07-15-7866

### Internal

- Push records to server during sync in batches
- Allow push and pull sync batch sizes to be configured separately

## 2021-07-13-7862

### Internal

- Migrate `OverdueAppointment` to add patients without phone number
- Migrate `kotlinx.android.parcel` usage to `kotlinx.parcelize`
- Change `CrashReporter` to be a facade instead of a discrete interface
- Show download and share buttons in overdue list
- Show invalid json error when `NHID` number is empty or less than 14 digits
- Explicitly pin OkHttp version to 4.X
- Migrate `RecentPatientsScreen` to a fragment
- Paginate recent patients list
- Fix overdue list item UI indentation
- Bump Kotlin to v1.5.20
- Increase tappable area of overdue call button
- Add `retainUntil` column to the `Patient` table
- Remove unnecessary method from `Analytics`
- Bump dependencies for [Flipper](https://fbflipper.com)
- Bump `com.github.egslava:edittext-mask` -> '1.0.7'
- Bump AGP to v4.2.2
- Replace `GITHUB_ACCESS_TOKEN` with `SERVICES_ACCESS_TOKEN`
- Clean up `gradle.properties`
- Extract product flavor build config fields into `defaultConfig`

### Changes

- Updated translations: `pa-IN`, `hi-IN`, `te-IN`, `kn-IN`, `mr-IN`, `te-IN`, `sid-ET`, `kn-IN`, `ta-IN`, `bn-BD`, `bn_IN`, `so-ET`, `ti-ET`, `am-ET`
  , `ta-LK`, `om-ET`
- Click on overdue patient to open patient summary
- Show progress when loading overdue patient contact information
- Tap outside or swipe to dismiss the bottom sheets
- Tap outside to dismiss the dialogs

### Fixes

- Fix invalid qr code error when scanning a valid Indian NHID

## 2021-07-02-7847

### Internal

- Use single list adapter in `InstantSearchScreen`

### Changes

- Added a question about hypertension treatment when creating patient

### Fixes

- Fix random crashes when instant search screen is in background

## 2021-06-28-7839

### Internal

- Use `ExperimentalGetImage` for `BitmapUtils#getBitmap`
- Bump Annotation library to v1.2.0
- Add Annotation experimental library
- Bump Room to v2.3.0
- Bump Paging to v3.0.0
- Prefill search query in `InstantSearchScreen`
- Paginate search results in `InstantSearchScreen`
- Replace usage of custom `Optional` class with `java.util.Optional`
- Add analytics events in teleconsult log after success
- Paginate overdue appointments list
- Fix instant search error views overlapping each other
- Remove usage of deprecated constructor from `MobiusDelegate`

### Features

- Add training videos depending on language for `Amharic` and `Oromo`

### Changes

- Updated translations: `hi-IN`, `bn-BD`, `te-IN`, `bn-IN`, `am-ET`, `pa-IN`, `ti-ET`, `kn-IN`, `mr-IN`, `ta-IN`, `so-ET`, `om-ET`
- New supported language: Sidama [`sid-ET`]
- Updated home screen illustrations

### Fixes

- Fix confirm reset pin error text is not hidden by default

## 2021-06-16-7826

### Internal

- Implement QR code JSON parser and extracted direct Moshi usage in `ScanSimpleIdEffectHandler`

## 2021-06-14-7821

### Internal

- Bump Kotlin to v1.5.10
- Bump AGP to v4.2.1
- Use `.editorconfig` for project code style
- Prefill NHID as `alternateId` in `PatientEntryScreen`
- Update facility picker search edit text hint
- Move `PatientPrefillInfo` to `patient` package to resolve coupling
- Add a new style and a theme overlay for `TextInputDatePicker` textfields
- Remove `ShortCodeResultsScreen`
- Deprecate `PatientRepository#search`
- Remove RxBinding2
- Use new method of SQL performance profiling (transform generated Room DAOs)
- Redact logs with `RecentPatient`

### Features

- Add NHID support in `PatientEntryScreen`
- Use `PatientPrefillInfo` for prefilling patient entry information
- Show NHID in `PatientEntryScreen`
- Highlight patient identifiers in search

### Fixes

- Fix user cannot see sync button
- Fix progress view for manual OTP entry screen is not hidden when login fails

## 2021-05-31-7801

### Internal

- Improve `ScanSimpleIdScreen` navigation
- Change labels to indicate the ID search
- Bump Mobius to v1.5.3
- Material Theming Migration
- Show patients with national health id when searching with from scan screen
- Remove old patient search
- Show `LinkIdWithPatientSheet` for Indian NHID
- Auto request reviewer for opened and re-opened PRs
- Update National Health ID strings
- Remove unwanted fields in `PatientPrefillInfo`
- Don't auto request review if a reviewer is already assigned
- Show error text when user scans invalid QR code
- Redact sensitive information from logs
- Implement Instant Search using BP Passport Number

### Features

- Add NHID support in `ScanSimpleIdScreen`
- Update title text and label in `ScanSimpleIdScreen`

### Changes

- Update translations: `ti-ET`, `pa-IN`, `om-ET`, `bn-IN`, `te-IN`, `hi-IN`, `am-ET`, `ta-IN`, `mr-IN`, `bn-BD`, `so-ET`, `kn-IN`
- Home screen improvements
  - Update home screen illustrations
  - Change patient search button text style

## 2021-05-17-7776

### Internal

- Add automatic performance profiling (reported to Mixpanel) for the Room database queries
- Show Indian national health ID in `PatientSummaryScreen`
- Bump Firebase dependencies
  - Firebase Performance Plugin to 1.3.5
  - Firebase Performance Client to 19.1.1
  - Google Services Plugin to 4.3.5
- Enable near realtime Firebase performance
  monitoring ([MORE INFO](https://firebase.google.com/docs/perf-mon/troubleshooting?authuser=0&platform=android#faq-real-time-data))
- Refactor `BpPassportSheet` to `ScannedQrCodeSheet`
- Show Indian national health ID in `EditPatientScreen`
- Migrate Gradle build scripts to use Kotlin DSL
- Stop reporting `SyncEvent` to analytics
- Bump RootBeer to v0.0.9
- Bump AGP to v4.2.0
- Bump Kotlin to v1.5.0
- Add a common color resource for all search string query highlights
- Bump SQLite version to 3.35.5
- Update `removeUntil` predicate in `Router#replaceKeyOfSameType`
- Fix attaching removed fragment to `FragmentTransaction` in `Router`
- Show error dialog when assigning a NHID to a patient with an existing ID

### Fixes

- Fix highlight color for instant search using phone number

## 2021-05-03-7756

### Features

- Add Instant Search using Patient Identifiers (Bangladesh National id, Ethiopian medical record no., etc)

### Internal

- Move QR code scanner preview view inside `ScanSimpleIdScreen`
- Remove dialog & sheet from navigation back stack when dismissing
- Return null for `Cursor.string` extension if the column index is less than or equal to -1
- Add `IndiaNationalHealthId` as an identifier in `IdentifierType`
- Add sql query for instant search by numeric criteria
- Bump Sentry to v4.3.0

### Changes

- Rename `Scan BP passport` button to `Scan QR code`
- Show remove overdue appointment reasons in a separate screen
- Show list of patients when multiple patients have same BP passport

### Fixes

- Fix dead patients showing in overdue list

## 2021-04-20-7712

### Internal

- Make entire prescribed drugs list scrollable
- Bump AGP to v4.1.3
- Bump Kotlin to v1.4.32
- Bump Dagger to v2.33
- Bump LeakCanary to v2.6
- Bump CameraX dependencies
- Bump Fragment to v1.3.2
- Bump PlayServices Auth to v19.0.0
- Bump Moshi to v1.11.0
- Bump JUnit to v4.13.2
- Change facility picker and instant search highlight background color to yellow
- Update Ethiopian date separator pattern
- Migrate `ContactPatientBottomSheet` to a `BaseBottomSheet`
- Add Material motion in `ContactPatientBottomSheet` for handling view visibility changes
- Migrate `ScheduleAppointmentSheet` to a `BaseBottomSheet`
- Migrate `PatientsTabScreen` to a fragment
- Migrate `ReportsScreen` to a fragment
- Migrate `OverdueScreen` to a fragment
- Add `DatePickerKeyFactory` for getting date picker key based on `Country`
- Move `TextInputDatePickerSheet` behind a feature flag
- Migrate app to use ViewBinding
- Replace `kotlin-android-extensions` with `kotlin-parcelize`

### Features

- Show `TextInputDatePickerSheet` for Ethiopian users
- Add Ethiopian calendar support

### Changes

- Add Somali language option to language selection
- Request camera permissions when opening QR code scanner from instant search screen if not provided

### Fixes

- Fix date stepper showing black color when it's disabled
- Fix prescribed drugs item corner radius not being updated when order is changed

## 2021-04-05-7697

### Internal

- Trim start and end white spaces for instant search query
- Add drug stock option in progress
- Show custom error screen for drug stock web view when there is no internet connection
- Remove date of birth `DateTimeFormatter` as constructor param from `PatientRepository`

### Features

- Open BP passport scanning from search screen

### Changes

- Update translations: `mr-IN`, `bn-BD`, `ti-ET`, `te-IN`, `pa-IN`, `hi-IN`, `om-ET`, `ta-IN`, `kn-IN`, `am-ET`, `bn-IN`
- Change "Preferred facility" to "Assigned facility"
- Show 'CHANGE' button in appointment reminder bottom sheet
- Highlight search field and show keyboard when 'Add to existing patient' option is selected
- [Ethiopia] Show dates in Ethiopian calendar

### Fixes

- Fix separator between Age & DOB fields not visible in patient entry & edit screens

## 2021-03-29-7688

### Internal

- Add feature flag for village type ahead search

### Fixes

- Fix village type ahead crash when creating/editing patient

## 2021-03-22-7681

### Internal

- Migrate `LinkIdWithPatientView` to `LinkIdWithPatientSheet`
- Convert `screen_patient_summary` to use `ConstraintLayout`
- Bump Play Core to v1.10.0
- Add option to manually trigger GH Actions
- Migrate `SetupActivity` to the new navigation framework
- Show link id sheet after patient summary profile is loaded and link id is present
- Use personal access token for rebase action
- Fix PR comment commands running condition
- Add recipe for integrating a new sync resource
- Bump ML Kit barcode dependency
- Bump CameraX dependencies
- BP passport scanner improvements
  - Added tap to focus in QR code scanner preview
  - Switch to `ZxingQrCodeAnalyzer` when ML Kit is unavailable

### Changes

- Updated translations: `bn-BD`, `te-IN`, `hi-IN`, `ta-IN`, `mr-IN`, `bn-IN`, `ti-ET`, `am-ET`, `pa-IN`, `om-ET`
- Show next button in the registration screens
- Implement village type ahead in patient entry/edit screens
- Show overdue count in home screen tabs
- Filter results in village type ahead in patient entry/edit screens

## 2021-03-15-7672

### Fixes

- Fix prescribed drugs list going beyond screen bounds

## 2021-03-08-7663

### Internal

- Load the current facility as a direct call instead of a reactive one in `OverdueEffectHandler`
- Migrate `PatientSummaryScreen` to a fragment
- Bump Dagger to v2.32
- Bump Google truth to v1.1.2
- Bump WorkManager to v2.5.0
- Fix the Room query thread pool executor to run all available threads
- Load list of `colonyOrVillage` from the `PatientAddress` table
- Change `TeleconsultRecordScreen` to a fragment
- Change `EditMedicinesScreen` to a fragment
- Only load the alphabetical patient log once when the screen is opened
- Add `Router#popUntilInclusive`

### Changes

- Updated translations: `bn-IN`, `ti-ET`, `om-ET`, `bn-BD`, `te-IN`, `kn-IN`, `mr-IN`, `hi-IN`, `pa-IN`, `am-ET`
- Show progress when linking bp passport to patient
- Show progress when saving medical history for new patient
- Change prescriptions end icon to a chevron icon instead of dropdown

## 2021-03-03-7655

### Internal

- Improve performance of the instant search queries by ~50%

### Changes

- Stop loading patients from other facilities in the alphabetical patient log

### Fixes

- Fix cannot exit patient summary screen after scheduling an appointment

## 2021-02-26-7647

### Internal

- Change `AppLockScreen` to a fragment

## 2021-02-22-7641

### Internal

- Bump Material Design Components to v1.3.0
- Bump ConstraintLayout to v2.0.4
- Add `Widget.Simple.TextField.Layout.PatientEntry.AutoComplete` style
- Bump Kotlin to v1.4.30
- Remove [JCenter](https://bintray.com/) from Maven repository sources
- Set patient status to `migrated` whenever an overdue appointment is removed for one of the following reasons
  - "Transferred to another public health facility"
  - "Moved to private practitioner"
- Build only the `PRODUCTION` release APK as part of the CI checks

### Changes

- Updated translations: `mr-IN`, `ta-IN`, `bn-BD`, `ti-ET`, `am-ET`, `kn-IN`, `bn-IN`, `hi-IN`, `te-IN`, `pa-IN`, `te-IN`, `om-ET`, `ta-IN`
- Add next button to phone number registration screen
- Change prescribed drugs toolbar title to `Medicines`
- Fix downloaded prescription background color
- Change UI for adding protocol/custom drugs
- Show toolbar progress indicator in facility picker screen
- Update sorting order of prescribed drugs

### Fixes

- Fix App crash when returning to the register patient screen before completing registration when patient date of birth is entered
- Fix prescription entry sheet UI
- Fix name string in link id with patient view

## 2021-02-11-7629

### Internal

- Disable strict mode crash for VM policy
- Redesign `LinkIdWithPatientView`
- Move to Dagger assisted inject
- Make instant search by name case insensitive
- Change `InstantSearchScreen` to a fragment
- Change `ScanSimpleIdScreen` to a fragment
- Change `HomeScreen` to a fragment
- Remove `ScanBpPassportActivity`
- Migrate `TheActivity` to the new navigation framework
- Add `CardViewDetector` lint for warning when using androidx/appcompat `CardView`
- Add `TextViewTextSizeDetector` lint for warning when using `TextView#textSize`
- Bump AGP to v4.1.2
- Fix bottom sheet dialog theme
- Add `NoopViewRenderer`
- Migrate `BpPassportSheet` to `BaseBottomSheet`
- Inject user's country code in `SentryCrashReporter`
- Migrate `ShortCodeSearchResultScreen` to a fragment
- Change `AccessDeniedScreen` to a fragment
- Remove deprecated Mobius delegate usage in `BloodSugarHistoryScreen`
- Migrate `TeleconsultSharePrescriptionScreen` to a fragment
- Change `AlertFacilityChangeSheet` to `BaseBottomSheet`
- Change `FacilityChangeScreen` to `BaseBottomSheet`
- Change `ConfirmFacilityChangeScreen` to `BaseBottomSheet`
- Change `PatientSearchResultsScreen` to a fragment

### Changes

- Updated the app icon

### Features

- Highlight patient name and number in Instant search

## 2021-01-25-7605

### Internal

- Rename `newbranch` script to `newbranch.sh`
- Purge the QA environment before running the instrumented tests instead of after in CI
- Add support for reporting screen changes to Analytics for the new navigation framework
- Migrate `ProgressBar` usage to `CircularProgressIndicator`
- Scroll to top when instant search results are updated
- Report time taken when loading all patients and search results in Instant Search
- Change `RegistrationPhoneScreen` to a fragment
- Change `RegistrationPinScreen` to a fragment
- Change `LoginPinScreen` to a fragment
- Change `RegistrationFullNameScreen` to a fragment
- Change `RegistrationConfirmPinScreen` to a fragment
- Show keyboard in Instant Search only when there is no identifier
- Add different adapters for showing all patients and search results in Instant Search
- Add ADR for the new navigation framework
- Use Dagger Factory to bind instances instead of `BindX` interfaces
- Bump Room dependency
- Bump CameraX dependency
- Bump Dagger dependency
- Clean up the abstract screen types
- Migrate `AuthenticationActivity` to the new navigation framework
- Improve instant search ordering

### Features

- Add Instant search

### Changes

- Updated translations: `bn-IN`, `mr-IN`, `hi-IN`, `pa-IN`, `bn-BD`, `am-ET`, `ti-ET`

## 2021-01-11-7589

### Internal

- Add shell env comment to `pre-push` hook
- Add the Router for the new navigation framework
- Track code style and lint rules for project in VCS
- Add Instant search query
- Add `ScreenFragmentCompat` to support using the older view-based screens in the v2 navigation framework
- Add convenience classes for creating screens for the new navigation framework

### Fixes

- Fix BPpassport prefill value to have display value

### Changes

- Change home illustration for India

## 2020-12-31-7576

### Fixes

- Fix short code search screen crash

## 2020-12-28-7571

### Internal

- Add PR comment commands for running instrumented tests and rebasing PR
- Add breadcrumbs for different stages in search operations
- Cleanup `ImageSrcDetector`
- Bump tooling JDK to 11.0
- Migrate `ShortCodeSearchResultScreen` to Mobius

### Changes

- Updated translations: `pa-IN`, `te-IN`, `ti-ET`, `bn-IN`, `mr-IN`, `am_ET`

### Fixes

- Fix a crash that could happen when closing the edit medicines screen

## 2020-12-17-7559

### Internal

- Convert the `ImageProxy` to `Bitmap` when decoding the QR code using ML kit

## 2020-12-14-7554

### Internal

- Bump appcompat -> 1.2.0
- Bump CI JDK version to 11
- Revert view binding migration for `RecentPatientsView`
- Add `MLKitQrCodeAnalyzer`

### Changes

- Disable the change language feature on devices running Lollipop (API level 21, 22)
- Updated translations: `kn-IN`, `ta-IN`, `pa-IN`, `om-ET`

### Fixes

- Fix `shortCodeText` auto focusing in `ScanSimpleIdScreen`
- Fix app going back to home screen after scanning a BP passport

## 2020-12-03-7536

### Fixes

- Fix BP passport scanning not working

## 2020-12-01-7532

### Internal

- Move BP passport scan results handling to `HomeScreen`

### Fixes

- Fixed BP Passport scanning does not work after the first scan

## 2020-11-30-7529

### Features

- Refill prescriptions in edit medicine screen
- Add support for recording RBS, FBS, and PPBS blood sugars in two units: mmol/L or mg/dL

### Internal

- Add `recipes.md`
- Added blood sugar unit preference
- Move scanning of QR codes to a discrete activity
- Bump AGP to 4.1.1
- Convert blood sugar values to `mg/dL` when saving/updating

### Changes

- Disabled running the app on rooted devices
- Change label of district address fields in patient entry and edit screens to "Zone" for Ethiopia users
- Updated translations: `ti-ET`, `pa-IN`, `ta-IN`, `te-IN`, `mr-IN`, `bn-IN`, `hi-IN`, `ta-IN`, `bn-BD`, `kn-IN`, `am-ET`
- Render blood sugar in `mmol/L` in summary view and history.
- Refill prescriptions in edit medicine screen

### Fixes

- Fix issue where the local user state can become inconsistent during registration

## 2020-11-09-7507

### Features

- Add `TeleconsultStatusSheet`
- Show `Next` in `ScheduleAppointmentSheet`

### Internal

- Update `TeleconsultRecord` to sync frequently
- Add `TeleconsultStatus` to `TeleconsultRecord`
- Mark strings used in prescription image as un-translatable
- Use `ENGLISH` locale when formatting prescription date
- Rename `TeleconsultRecord` `requestCompleted` to `requesterCompletionStatus`
- Bump the resync token for the Facility sync
- Bump AGP to 4.1.0
- Add `PrescribedDrug#refill` method
- Remove unnecessary sealed class types for different Business ID metadata versions
- Clean up `PatientRepository`
  - Change reactive calls for registering a patient to synchronous ones
  - Accept the ongoing patient entry as a parameter to the register patient method
  - Remove deprecated `Optional` class usages
- Move registration and login flows to a separate activity
- Update `appupdatedialog_body` string
- Enable ViewBinding
- Migrate `SplashScreen` to use ViewBinding

### Changes

- Update translations: `kn-IN`, `ta-IN`, `bn-IN`, `ti`, `bn-BD`, `pa-IN`, `mr-IN`, `te-IN`, `hi-IN`

### Fixes

- Fix teleconsult button in patient summary screen is broken on some devices

## 2020-10-26-7490

### Features

- Add `SelectDiagnosisErrorDialog` when diagnosis is not selected in `NewMedicalHistoryScreen` and `MedicalHistorySummaryView`

### Changes

- Made "SMS Reminders" to be the default consent label on the patient screen unless otherwise specified on a country level
- Show toast message after saving prescription image on device
- Fix `ProgressMaterialButton` AVD not animating when initial state is not `in-progress`

### Internal

- Add `syncGroup` property to the `Facility` resource
- Remove custom WorkManager initializer
- Bump Android Gradle Plugin version to 4.0.2
- Add method to `AppDatabase` to clear all patient data not in the current sync group
- Update download and share `MaterialButtons` in `TeleconsultSharePrescriptionScreen` to `ProgressMaterialButtons`
- Purge unused data after a full sync completes
- Register patients in sync integration tests
- Stop registering a blood pressure measurement to associate a patient in tests with a facility
- Report database optimization events to analytics
- Delay purging of data from a different sync group for a fixed duration (remotely configurable) after switching facilities
- Add support for server controller resync when switching facility sync groups

### Fixes

- Fix app freeze when pressing enter/done with empty pin in lock screen

## 2020-10-13-7476

### Features

- Add `TeleconsultPrescriptionScreen`
- Add `TeleconsultSharePrescriptionScreen`

### Changes

- Updated translations: `bn-IN`, `ti-ET`, `bn-BD`, `ta-IN`, `pa-IN`, `kn-IN`, `mr-IN`, `om-ET`
- Switch the positions of the teleconsultation and Save buttons in the `PatientSummaryScreen`

### Internal

- Remove `PrescribedDrugs` from `TeleconsultRecord`
- Create Kotlin extension for saving Optional preferences
- Modify `PrescribedDrug` to include `teleconsultation_id`
- Change `BloodPressureEntryEffectHandler` create and update measurement flows to be mostly synchronous
- Remove v1 of the QR code scanning flow
- Add custom traces in Firebase Performance Monitoring for the following flows:
  - Record new blood pressure measurement
  - Update existing blood pressure measurement
- Change registration and current facility ID columns to regular columns without foreign keys in `User`
- Add teleconsult record sync
- Change `RemoteConfigService` to pull updates synchronously
- Stop injecting screens in `TheActivity` via the static component
- Add support for sending teleconsult request via SMS
- Show medicines required error in `TeleconsultPrescriptionScreen`
- Remove `TeleconsultationApi` & load medical officers from `MedicalOfficer` table
- Fix prescribed drug frequency type adapter
- Request storage permission when downloading or sharing prescription
- Fix small UI inconsistencies in share prescription screen
- Hide keyboard when exiting from `TeleconsultPrescriptionScreen`
- Add file provider for sharing prescription image below Android 10

### Fixes

- Fix deeplink screens & warning dialogs displaying again after activity restart.
- Fix home screen tabs swiping
- Fix issue where sync events where being triggered on an individual sync level rather than at a group level

## 2020-10-01-7452

### Fixes

- App lock screen does not show if the app is exited and opened again while it is on the lock screen

## 2020-09-28-7446

### Features

- Add `TeleconsultRecordScreen`
- Show `TeleconsultLogNotAllowed` dialog when Medical officer is not allowed to teleconsult

### Internal

- Add `Capabilities` to User
- Change `TeleconsultFacilityInfoApi` endpoint
- Add methods to purge soft-deleted records from the database
- Add method to purge cancelled and visited appointments from the database
- Bump target SDK version to 30
- Add weekly recurring task to run maintenance tasks on the database
- Integrated Firebase Performance Monitoring

### Changes

- Updated translations for: `om-ET`, `ta-IN`, `bn-BD`, `mr-IN`, `hi-IN`, `ti-ET`
- Add ripple on touch for overdue patient name
- Set drug duration max limit

## 2020-09-15-7432

### Changes

- Open patient summary screen when patient name is clicked in overdue
- Stop loading count of overdue items on the home screen

### Internal

- Use UiRenderer for setting drug duration in `DrugDurationSheet`
- Add teleconsultation facility sync (Disabled for now until API endpoint is live: ETA 04-09-2020)
- Added `MedicineFrequencyBottomSheet` to update medicine frequency
- Add support for the teleconsultation record deeplink
- Add `frequency` & `durationInDays` to `PrescribedDrug`
- Add `ImageSrcDetector` lint for warning when using `ImageView#src`
- Add `TeleconsultRecord` and `TeleconsultRecordPayload`
- Bumped internal SQLite version to 3.32.2
- Change saving of app lock timestamp to an in-memory value
- Pin the number of threads used for running queries in Room based on the SQLite connection pool size
- Disable state saving and restoration for the search results view
- Stop querying for redundant facility in `LoggedInUserHttpInterceptor`

### Fixes

- Fixed BloodPressure removal not working

## 2020-09-03-7417

### Fixes

- Add a default RxJava error handler to ignore some classes of errors safely

## 2020-09-02-7414

### Fixes

- Fixed issue where patient details would not load for patients without an assigned
  facility ([#1127](https://app.clubhouse.io/simpledotorg/story/1127/patient-details-screen-does-not-load-for-patients-who-don-t-have-an-assigned-facility))

## 2020-09-01-7409

### Changes

- Added `SignatureActivity` to accept and save user signature
- Increase error text sizes
- Update error message of address fields in `PatientEntryScreen`

### Internal

- Migrated `LoggedOutOfDeviceDialog` to Mobius
- Migrated `ConfirmRemoveBloodPressureDialog` to Mobius
- Migrate `UpdatePhoneNumberDialog` to Mobius
- Migrated `LinkIdWithPatientView` to Mobius
- Migrate `PatientSearchResultsScreen` to Mobius
- Migrated `TheActivity` to Mobius
- Migrated `AppLockScreen` to Mobius
- Migrated `PatientSearchView` to Mobius
- Added `TeleConsultSuccessScreen` to notify teleconsultation recorded successfully
- Add `teleconsultPhoneNumber` in `User`, `LoggedInUserPayload` & `OngoingLoginEntry`
- Change `HomeScreen` to save and restore the state correctly
- Improved the user experience of loading the patient summary screen
- Optimized fetching of current facility by removing the unnecessary `User` parameter
- Clean up sync tests
- Change syncs to happen on a fixed pool of threads

## 2020-08-24-7397

### Fixes

- Fixed native crash on Android 9 with animated vector
  drawables ([#364](https://app.clubhouse.io/simpledotorg/story/364/fix-vectordrawable-native-crash))
- Fixed crash on putting the app in the background during registration facility
  selection ([#1030](https://app.clubhouse.io/simpledotorg/story/1030/app-crashes-during-registration-when-it-is-backgrounded))

## 2020-08-18-7386

### Features

- Add progress UI for registering a patient
- Add progress UI when scheduling appointment in `ScheduleAppointmentSheet`
- Add progress UI when editing a patient in `EditPatientScreen`
- Add progress UI when saving Blood pressure measurements in `BloodPressureEntrySheet`
- Add progress UI when saving Blood Sugar in `BloodSugarEntrySheet`

### Changes

- Updated translations: `ti`
- Update error message of address fields in `EditPatientScreen`

### Internal

- Migrated `LoginPinScreen` to Mobius
- Extracted facility selection UI into a separate component
- Migrated `ForgotPinCreateNewPinScreen` to Mobius
- Remove `AddToPatientSearch` screens and merge them with `PatientSearch` screens
- Migrated `OverdueScreen` to Mobius
- Migrated `FacilitySelectionActivity` to Mobius
- Migrated `FacilityChangeActivity` to Mobius
- Migrated `DrugSummaryView` to Mobius
- Migrated `ForgotPinConfirmPinScreen` to Mobius
- Change `OverdueScreen` to load data via the paging library
- Migrated `PatientSearchScreen` to Mobius
- Migrated `HelpScreen` to Mobius
- Migrated `HomeScreen` to Mobius
- Migrated `ScanSimpleIdScreen` to Mobius
- Migrate `EnterOtpScreen` to Mobius
- Setup the UI via an effect in `PatientEntryScreen`
- Setup the UI via an effect in `EditPatientScreen`
- Separate input fields providers based on country
- Add separate labels for "Street address" and "Village/Colony/Ward" labels in `PatientEntryScreen` and `EditPatientScreen` for Chennai facility
  groups

### Fixes

- Fixed issue where recent and overdue patients on home screen would not update after changing
  facility ([#742](https://app.clubhouse.io/simpledotorg/story/742/home-screen-does-not-update-when-changing-facilities))
- Fixed issue where the app would crash on restoring the home screen
  state ([#791](https://app.clubhouse.io/simpledotorg/story/791/the-app-crashes-when-navigating-back-to-the-home-screen-from-any-other-screen))
- Fix patient summary not opening on item click in all recent patients screen

## 2020-08-03-7364

### Features

- Show assigned facility in the patient summary screen
- Use patient's assigned facility to schedule an appointment
- Show `Transferred to` label in Overdue screen

### Internal

- Use `app:srcCompat` for `ImageView`
- Migrated `ReportsScreen` to Mobius
- Migrated `AddPhoneNumberDialog` to Mobius
- Migrated `RegistrationFacilitySelectionScreen` to Mobius
- Migrated `RecentPatientsView` to Mobius
- Migrated `RegistrationLoadingScreen` to Mobius
- Change selection of facility throughout the codebase to use `ItemAdapter`
- Bump patient resync token
- Migrated `RecentPatientsScreen` to Mobius
- Remove tracking of ongoing registration entry from `UserSession`

### Changes

- Updated translations: `bn_IN`, `am_ET`, `te_IN`, `pa_IN`, `hi_IN`, `bn_BD`
- Added Registration Facility name and date in `PatientSummaryScreen`
- Exclude deleted facilities from facility selection sheets
- Load overdue patients if current facility is same as patient assigned facility

### Fixes

- Fix crash when removing phone number for a patient that already has
  one ([LINK](https://app.clubhouse.io/simpledotorg/story/366/app-crashing-when-phone-number-is-empty-while-editing-patient))

## 2020-07-23-7353

### Internal

- Migrated `RegistrationPinScreen` to Mobius
- Migrated `ConfirmRemovePrescriptionDialog` to Mobius
- Migrated `RegistrationConfirmPinScreen` to Mobius
- Updated CI checks to look for [Clubhouse](https://app.clubhouse.io/simpledotorg) tickets instead of Pivotal Tracker
- Migrated `RegistrationLocationPermissionScreen` to Mobius
- Added patient registration and assigned facility columns to the patient model
- Replaced [ThreeTenBp](https://www.threeten.org/threetenbp/) for time APIs
  with [`java.time`](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
- Enable vector drawable support library flag in app gradle

### Changes

- Add support for Medical Record Number in Ethiopia
- Add Amharic translation
- Updated translations: `mr_IN`, `om_ET`, `hi_IN`, `pa_IN`, `bn_BD`, `te_IN`, `ti_ET`

### Fixes

- Fixed occasional crash when starting a teleconsultation
  session ([LINK](https://app.clubhouse.io/simpledotorg/story/414/starting-a-teleconsultation-session-crashes-in-some-scenarios))

## 2020-07-07-7330

### Changes

- New Onboarding UI
- Show new registration label for recent patients in home screen
- Redesigned patient card in search results
- Changed blood sugar unit from `mg/dl` to `mg/dL`
- Show blood sugar high/low level labels
- Redesigned patient card in recent patients screen
- New illustrations for location permission screen, overdue screen, reports screen
- Show overdue appointment count badge in overdue tab
- Changed normal call buttons in the app from green to blue
- Updated translations: `bn_BD`, `kn_IN`, `mr_IN`, `am_ET`
- Ethiopia phone number validation

### Internal

- Migrated `RecentPatientItemType` to use `ItemAdapter`
- Moved storage of cached webviews (Progress, Help) to an implementation backed by the local database instead of files
- Migrated `RegistrationFullNameScreen` to Mobius
- Migrated `RegistrationPinScreen` to Mobius
- Make non-protocol drugs immutable
- Refactored logic around fetching location in facility selection screens to a common class
- Decoupled feature toggles from the remote configuration setup

### Fixes

- Fixed retrying on a failed teleconsultation fetch when in airplane mode
- Fixed showing diagnosis text in teleconsult message when diagnosis are unanswered
- Fixed keyboard not opening in app lock screen
- Fixed prescription created from a protocol drug not-showing in `EditMedicinesScreen`
- Fix changing of appointment facility does not update the facility

## 2020-06-22-7314

### Feature

- User can soft delete patient
- Add diagnosis and blood sugar readings to the teleconsultation message
- User can now select from multiple doctors when requesting a teleconsultation

### Changes

- Rename the button label to 'Next' when assigning a new BP passport to an existing patient
- Add visual indication for low blood sugars
- Updated translations: `ti_ET`, `om_ET`, `am_ET`

### Internal

- Change syncs on login to trigger at the usage site instead of in the `LoginUserWithOtp` class
- Support receiving multiple teleconsultation phone numbers
- Introduce a `UUID` generator class to ease testing record creation
- Migrated `RegistrationPhoneScreen` to Mobius
- Add support for [Java 8 desugaring](https://developer.android.com/studio/write/java8-support)
- Implement [Java `Optional`](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) APIs on the project `Optional` type
- Implement `ProgressMaterialButton`

### Fixes

- Fix blood pressure "High" label displaying when blood pressure is low.

## 2020-06-08-7298

### Feature

- Handle cases where the patient ID is not set correctly in the deeplink
- Show address for overdue patient

### Internal

- Migrated `ScheduleAppointmentSheet` to Mobius
- Fix PR description CI when using markdown syntax
- Add `DeletedReason` for `Patient`
- Improved analytics around sync events
- Updated translations: `bn_BD`
- Tighten Code Climate checks
- Add support for Android Studio 4.0
- Add support for building Android App Bundles (AABs)
- Migrate `PatientsScreen` to Mobius
- Migrated `SyncIndicatorController` to Mobius
- Add extension for extracting intent data from `ActivityResult`

### Fixes

- Fix camera crash when QR scanner view is paused/closed
- Fix fetching teleconsultation error when `PatientSummaryScreen` is restored

## 2020-05-21-7276

### Feature

- Add support for teleconsultation & deep-linking to open patient summary.

### Internal

- Lazy inject `facility` and `user` in all `EffectHandler` classes

### Fixes

- Fix the click issue in custom prescription on Medicines screen. [LINK](https://www.pivotaltracker.com/story/show/172784091)

## 2020-05-18-7270

### Feature

- Add support for calling the patient directly from the summary screen
- Show all BP passports of a patient while editing them
- New design for calling patient from the overdue list

### Internal

- Replace flow with navigation component in `SetupActivity`
- Updated translations: `bn_BD`, `kn_IN`, `hi_IN`, `mr_IN`, `pa_IN`
- Add CI check for fixing ellipsis's in strings.xml
- Extracted `BloodPressureReading` model as an embedded model from `BloodPressureMeasurement`
- Migrated `MedicalHistorySummaryView` to Mobius
- Add teleconsultation fields to the facility model
- Migrated `DosagePickerSheet` to Mobius
- Migrated `CustomPrescriptionEntryController` to Mobius

## 2020-05-04-7255

### Internal

- Add CI check for verifying that the CHANGELOG is updated
- Paginate data in blood sugar history page
- Use database view for `PatientSearchResult`
- Migrate `PrescribedDrugsScreenController` to Mobius

### Fixes

- Fixed how empty search error is shown

## 2020-04-20-7238

### Feature

- Introduce a facility confirmation screen whenever the user tries to perform one of the following actions immediately after changing facilities:
  - Register a new patient
  - Edit an existing patient
  - Schedule an appointment for a patient
  - Record a blood pressure measurement
  - Record a blood sugar measurement
  - Change the prescription
- Introduce a confirmation screen during user sign-up after selecting a facility

### Fixes

- Fixed bug where the 'Help" button on the home screen would distort if the facility name was very long
- Fixed occasional crash when searching for a patient by phone number

### Internal

- Sync help and progress reports when the language is changed in Settings
- Change the default appointment schedule period to 28 days
- Remove Whatsapp reminders from the reminder notification consent form when registering a patient for Bangladesh users
- Hide the secure calling toll-free message if the secure calling feature is not available
- Phone numbers are no longer hidden in search results
- Migrated to the new secure user login API endpoints
- Introduced an alternate QR code scanning module, built on AndroidX. This is controlled by a feature flag (`use_new_qr_scanner`)
- Hide the "Transgender" gender option during patient registration for Ethiopia users
- Add support for the Ethiopia address model
- Change the date format for recording blood sugars and blood pressures to "DD/MM/YYYY"

## 2020-03-30-7201

### Feature

- Add support for recording HbA1c blood sugars
- Add a diagnosis label on the overdue appointments

## 2020-03-30-7201

### Feature

- Add support for recording HbA1c blood sugars
- Add a diagnosis label on the overdue appointments

### Internal

- Changed api versioning to be at the endpoint level rather than global
- Integrated tool to report flaky instrumentation tests
- Changed automatic appointments to be scheduled when a blood sugar has been recorded in addition to a BP
- Consider patients with only blood sugars recorded in the following screens:
  - All patients in facility
  - Search results when searching by name or phone number
- Trigger syncs automatically on the following conditions:
  - An appointment is scheduled from the patient summary screen
  - A patient is registered
- Bump blood sugar sync api version to v4
- Ask the user to select a diagnosis for patients who don't have both diagnosis questions answered in patient summary

### Fixes

- Fixed occasional crash where the application would resume from background

## 2020-03-09-7161

### Feature

- Added support for editing blood sugar
- Added a new language: Afaan Oromo

### Internal

- Bumped AGP to 3.6.1
- Switched to R8 for minifying the APK
- Removed spellfix sqlite extension
- Unified mobius view and activity delegate

### Fixes

- Fixed crash when trying to edit patient DOB

## 2020-02-12-7090

### Feature

- Introduce a new design for the patient details page
- Add support for recording the patient diagnosis, based on the facility diabetes management support
- Added sync support for the recorded blood sugars
- Display the complete address on the patient search results and patient details page
- Add support for deleting the Bangladesh National ID from the edit patient page
- Add support for adding a Bangladesh National ID from the edit patient page
- Order the patient's prescribed drugs alphabetically on the patient details page

### Internal

- Simplify the computation of high risk label in overdue list
- Use the diabetes management flag from the facility instead of the remote config flag to toggle the diabetes view on patient summary
- Updated to the new visual styling of the CV history, Blood pressure, and Blood sugar widgets on the patient summary screen
- Bump the resync token for CV history sync to 2
- Bump the resync token for facility sync to 2

### Fixes

- Fix issue where the street and zone address fields would not be synced to the servers
- Fix issue where the units('days', 'weeks') in the appointment reminder sheet would not be translated
- Fix issue where formatted numerals in the app (BP, Age, Overdue days, etc) would use numeric glyphs in some languages instead of Arabic numerals

## 2020-01-27-7040

### Feature

- Introduce Diabetes management for tracking patient blood sugar

### Fixes

- Fix blank Bangladesh national ID getting saved while editing a patient

## 2020-01-14-7007

### Feature

- Updated patient entry consent text to include registration of minors
- Added/updated translations for the following languages: Telugu, Punjabi, Marathi, Kannada, Hindi

### Fixes

- Patient shows up on the Recent patients list of a transferred facility as soon as they are transferred
- Blank national ID gets generated for patients, which stops them from syncing

### Internal

- Removed the remote homescreen illustrations feature
- Bumped resync token for appointment sync
- Fixed incorrect migration for appointments which set all appointment types to `manual`
- Moved to the dynamic address field model for patient editing

## 2020-01-01-6917

### Feature

- Transfer patients to a different facility for follow-up

## 2019-12-16-6732

### Fixes

- Fixed the broken UI on the phone call sheet (https://www.pivotaltracker.com/story/show/169860653)

### Internal

- Use the selected country for connecting to the server
- Moved to the dynamic address field model for patient entry

## 2019-12-03-6640

### Internal

- Remove old edit patient screen
- Add a feature toggle for the phone masking feature
- Disable phone masking feature by default
- Remove Heap analytics

### Feature

- Add a country selection screen during the registration

## 2019-11-18-6468

### Internal

- Move Blood Pressure Entry sheet to Mobius
- Add breadcrumbs for tracking sizes of view saved states

## 2019-10-29-6197

### Feature

- Add a loading screen when the app database is being migrated after an update

### Internal

- Remove old AllPatientsInFacilityView code
- Make changing of screens in Flow synchronous
- Introduce an alternate activity (SetupActivity) as the launcher activity
- Move onboarding screen to the SetupActivity

## 2019-10-21-6057

### Feature

- Add consent text when registering a patient

### Internal

- Home screen banner illustrations are fetched remotely instead of hardcoded
- Migrate patient edit screen to Mobius
- Migrate patient new entry screen to Mobius
- Migrate onboarding screen to Mobius

## 2019-10-11-5948

### Internal

- Add remote toggle for screen change animations
- Add instrumentation for Flow events

## 2019-10-07-5787

### Feature

- Limit editing of BP measurements to one hour after being recorded. This is remotely configurable.

### Fixes

- Stop opening BP entry sheet immediately after registering a new patient.

### Internal

- Migrate AllPatientsInFacilityView to Mobius

## 2019-09-23-5587

### Feature

- Show Simple video for training user until some patients are registered

### Internal

- Trigger a full sync when the application starts

## 2019-09-09-5430

### Feature

- Exclude deleted patient records while searching by name or phone number
- Exclude deleted patient records when loading the alphabetical patient log
- Exclude deleted patients when loading overdue appointments
- Exclude deleted patients when finding patient by scanning BP Passport
- Exclude deleted appointments when loading overdue appointments
- Update designs for patient summary screen
- Show loader while patient search results load
- Add hindi translations to the app
- Search by short code from Scan BP passport screen

### Internal

- Add Mixpanel analytics
- Add device locale, timezone, and timezone offset headers to all requests

## 2019-08-26-5229

### Feature

- Search by phone number support from the screen to find patient after scanning a BP Passport
- Older devices will automatically get cleared when the user logs in on a new device
- An exact date to schedule a patient visit can be picked from the schedule appointment screen

### Fixes

- Fix searching by phone number shows duplicate results for patients with multiple BPs

## 2019-08-12-5048

### Feature

- Search by phone number support from the screen to find patient without scanning
- Add a loading screen for registration
- Handle changes in user sync approval status

### Internal

- Remove unused Communication model
- Remove deprecated `validate` function and `Result` enum from `UserInputDateValidator`

### Fixes

- Fix taking the user to the production app play store page when install app update is clicked from sandbox or demo build
- Fix crash on overdue list when scrolling quickly when the screen is still loading appointments

## 2019-07-29-4866

### Feature

- All patients that have visited the current facility will be shown in the patient search screen
- Make BP date entry optional

### Internal

- Added forward compatibility support to MedicalHistory.Answer enum
- Added forward compatibility support to Gender enum
- Added forward compatibility support to PatientPhoneNumberType enum

### Fixes

- Fix camera not getting released properly and causing memory leaks
- Fix patient details not showing up sometimes in the phone call screen

## 2019-07-15-4657

### Feature

- Nudge users to update the app if it is older than expected

### Internal

- Change `LoggedInUser#status` to be a forward-compatible enum

### Fixes

- Fix overdue list to show last recorded BP, instead of last updated BP

## 2019-07-01-4522

### Feature

- Show "Last Seen" time of patient in recent patients list instead of "Last BP"

### Internal

- Change PinEntryCardView to accept PIN digest to verify externally instead of reading from UserSession

## 2019-06-17-4397

- Show 10 recent patients on the home screen, and the rest on a separate screen
- While calling patients, nurses can choose between phone call or secure anonymous call

## 2019-05-13-4013

- Added the BP Passport feature to the app
  - Scan a BP Passport and lookup a patient
  - Scan a BP Passport and link it with an existing patient
  - Scan a BP Passport and register a new patient

## 2019-04-22-3795

- Recent Patients list will not get affected by blood pressures and medical history entries that have been deleted
- Add a "Help" screen which is accessible from the home screen
- Show patient search results in two sections: ones who have visited the current facility, and the ones who have not

## 2019-03-25-3403

- Suggest nearby facilities to nurse, if switching after logging in
- Updated patient summary screen design
- Show “recent patients” on the home screen
- Sync status changes to “Pending” when patient data is updated
- Hide the sync indicator if nurse hasn’t been approved

## 3269-18Mar

- While creating a new account, facilities located nearby are automatically suggested
- Scheduling an appointment shows new date options

## 2856-25Feb

- Updated illustration on the patients tab
- Improved app performance during first sync of patient data
- Improve design of medical history screen

## 2605-04Feb

- Blood pressures can be entered for dates in the past
- New prescription drugs screen design
- New medicine dosage picker design

## 2301-04Jan

- Age and date of birth fields removed from patient search screen
- Improved patient search algorithm
- Added new cancellation reasons for appointments
- Updated the rules for identifying high-risk patients

## 2204-26Dec

- Fix: medical history syncing broke for some users

## 2116-17Dec

- Automatically submit PIN when 4 digits have been entered
- Show Yes/No/Unanswered buttons on medical history questions
- Show option to change clinics from the home screen
- Ask for confirmation when exiting Edit Patient screen without saving the edited information

## 2052-10Dec

- Dismiss keyboard while scrolling list of clinics
- Highlight search query term when filtering clinics

## 1991-03Dec

- Added ability to request a new OTP via SMS, while logging in
- Blood pressure values are now editable for 24 hours
- Demographic data of patients is now editable
- "Very High" and "Extremely High" BPs are now shows as "High"
- PIN entry is now protected against brute force attacks
- High risk patients are labelled in the overdue list
- Fix: crash when app was closed before entering OTP, while logging in

## 1705-10Nov

- Patient searches with more than 100 results caused a crash
- Months in a date were being ignored, when calculating fuzzy age bounds

## 1678-08Nov

- Patient entry UI was cleaned up; entering a colony is now mandatory.
- Visual updates to the patient summary and home screen.
- Patients can be marked as "Dead" on the overdue screen, after which they will stop showing up on the overdue screen.
- Added option to schedule appointment for one month.
- Change age search to be more permissive for older patients.
- Sync frequency has been increased to once every fifteen minutes.
- Fix: Occasionally certain records did not get synced to the server.

## 1462-16Oct

- Add a helpful message to the home screen
- Fix: calling a patient from the overdue list sometimes used an incorrect phone number

## 1420-11Oct

- Order facilities alphabetically in lists

## 1377-09Oct

- Fix: crash on opening patient summary if there was a BP recorded more than 6 months ago

## 1356-07Oct

- Fix: crash if multiple histories are present for the same patient
- Fix: crash if empty systolic or diastolic blood pressure value is submitted
