package org.simple.clinic.patient

enum class PatientEntryValidationError(val analyticsName: String) {
  PERSONAL_DETAILS_EMPTY("Patient Entry:No personal details entered"),
  FULL_NAME_EMPTY("Patient Entry:Name is empty"),
  PHONE_NUMBER_NON_NULL_BUT_BLANK("Patient Entry:Phone Number is empty"),
  PHONE_NUMBER_LENGTH_TOO_SHORT("Patient Entry:Phone Number is less than 6 digits"),
  PHONE_NUMBER_LENGTH_TOO_LONG("Patient Entry:Phone Number is more than 12 digits"),
  BOTH_DATEOFBIRTH_AND_AGE_ABSENT("Patient Entry:Age and DOB are both absent"),
  BOTH_DATEOFBIRTH_AND_AGE_PRESENT("Patient Entry:Age and DOB are both present"),
  INVALID_DATE_OF_BIRTH("Patient Entry:Invalid DOB"),
  DATE_OF_BIRTH_IN_FUTURE("Patient Entry:DOB in future"),
  MISSING_GENDER("Patient Entry:Gender missing"),

  EMPTY_ADDRESS_DETAILS("Patient Entry:Empty address details"),
  COLONY_OR_VILLAGE_EMPTY("Patient Entry:Colony or village empty"),
  DISTRICT_EMPTY("Patient Entry:District empty"),
  STATE_EMPTY("Patient Entry:State empty"),
  AGE_EXCEEDS_MAX_LIMIT("Patient Entry: Age greater than 120 years is not allowed"),
  DOB_EXCEEDS_MAX_LIMIT("Patient Entry: Age greater than 120 years is not allowed"),
  AGE_EXCEEDS_MIN_LIMIT("Patient Entry: Age equal to 0 is not allowed"),
  DOB_EXCEEDS_MIN_LIMIT("Patient Entry: Age equal to 0 is not allowed")
}
