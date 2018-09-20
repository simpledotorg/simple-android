package org.simple.clinic.search

enum class PatientSearchValidationError(val analyticsName: String) {
  FULL_NAME_EMPTY("Patient Search:Name is empty"),
  BOTH_DATEOFBIRTH_AND_AGE_ABSENT("Patient Search:Age and DOB are both absent"),
  BOTH_DATEOFBIRTH_AND_AGE_PRESENT("Patient Search:Age and DOB are both present"),
  INVALID_DATE_OF_BIRTH("Patient Search:Invalid DOB"),
  DATE_OF_BIRTH_IN_FUTURE("Patient Search:DOB in future"),
}
