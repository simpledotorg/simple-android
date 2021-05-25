package org.simple.clinic.patient

sealed class PatientEntryValidationError(val analyticsName: String) {
  object PersonalDetailsEmpty : PatientEntryValidationError("Patient Entry:No personal details entered")
  object FullNameEmpty : PatientEntryValidationError("Patient Entry:Name is empty")
  object PhoneNumberNonNullButBlank : PatientEntryValidationError("Patient Entry:Phone Number is empty")
  data class PhoneNumberLengthTooShort(val limit: Int) : PatientEntryValidationError("Patient Entry:Phone Number is less than 6 digits")
  data class PhoneNumberLengthTooLong(val limit: Int) : PatientEntryValidationError("Patient Entry:Phone Number is more than 12 digits")
  object BothDateOfBirthAndAgeAbsent : PatientEntryValidationError("Patient Entry:Age and DOB are both absent")
  object BothDateOfBirthAndAgePresent : PatientEntryValidationError("Patient Entry:Age and DOB are both present")
  object InvalidDateOfBirth : PatientEntryValidationError("Patient Entry:Invalid DOB")
  object DateOfBirthInFuture : PatientEntryValidationError("Patient Entry:DOB in future")
  object MissingGender : PatientEntryValidationError("Patient Entry:Gender missing")

  object EmptyAddressDetails : PatientEntryValidationError("Patient Entry:Empty address details")
  object ColonyOrVillageEmpty : PatientEntryValidationError("Patient Entry:Colony or village empty")
  object DistrictEmpty : PatientEntryValidationError("Patient Entry:District empty")
  object StateEmpty : PatientEntryValidationError("Patient Entry:State empty")
  object AgeExceedsMaxLimit : PatientEntryValidationError("Patient Entry: Age greater than 120 years is not allowed")
  object DobExceedsMaxLimit : PatientEntryValidationError("Patient Entry: Age greater than 120 years is not allowed")
  object AgeExceedsMinLimit : PatientEntryValidationError("Patient Entry: Age equal to 0 is not allowed")
  object DobExceedsMinLimit : PatientEntryValidationError("Patient Entry: Age equal to 0 is not allowed")
}
