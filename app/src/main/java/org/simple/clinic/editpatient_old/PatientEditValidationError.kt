package org.simple.clinic.editpatient_old

enum class PatientEditValidationError(val analyticsName: String) {
  FULL_NAME_EMPTY("Patient Edit:Name is empty"),
  PHONE_NUMBER_EMPTY("Patient Edit:Phone Number is empty"),
  PHONE_NUMBER_LENGTH_TOO_SHORT("Patient Edit:Phone Number is less than 6 digits"),
  PHONE_NUMBER_LENGTH_TOO_LONG("Patient Edit:Phone Number is more than 12 digits"),
  COLONY_OR_VILLAGE_EMPTY("Patient Edit:Colony or village empty"),
  DISTRICT_EMPTY("Patient Edit:District empty"),
  STATE_EMPTY("Patient Edit:State empty"),
  BOTH_DATEOFBIRTH_AND_AGE_ABSENT("Patient Edit:Both age and DOB are absent"),
  INVALID_DATE_OF_BIRTH("Patient Edit:Invalid DOB"),
  DATE_OF_BIRTH_IN_FUTURE("Patient Edit:DOB in future")
}
