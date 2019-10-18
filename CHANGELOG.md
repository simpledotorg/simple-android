# CHANGELOG

## Next Release
## Internal
- Remove old AllPatientsInFacilityView code
- Make changing of screens in Flow synchronous

## On Demo
## Feature
- Add consent text when registering a patient

## Internal
- Homescreen banner illustrations are fetched remotely instead of hardcoded
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
