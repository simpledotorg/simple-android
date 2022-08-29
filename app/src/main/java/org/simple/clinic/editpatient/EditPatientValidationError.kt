package org.simple.clinic.editpatient

sealed class EditPatientValidationError(val analyticsName: String) {
  object FullNameEmpty : EditPatientValidationError("Patient Edit:Name is empty")
  object PhoneNumberEmpty : EditPatientValidationError("Patient Edit:Phone Number is empty")
  data class PhoneNumberLengthTooShort(val minimumAllowedNumberLength: Int) : EditPatientValidationError("Patient Edit:Phone Number is less than 6 digits")
  object ColonyOrVillageEmpty : EditPatientValidationError("Patient Edit:Colony or village empty")
  object DistrictEmpty : EditPatientValidationError("Patient Edit:District empty")
  object StateEmpty : EditPatientValidationError("Patient Edit:State empty")
  object BothDateOfBirthAndAgeAdsent : EditPatientValidationError("Patient Edit:Both age and DOB are absent")
  object DateOfBirthParseError : EditPatientValidationError("Patient Edit:Invalid DOB")
  object DateOfBirthInFuture : EditPatientValidationError("Patient Edit:DOB in future")
  object AgeExceedsMaxLimit : EditPatientValidationError("Patient Edit: Age greater than 120 years is invalid")
  object DateOfBirthExceedsMaxLimit : EditPatientValidationError("Patient Edit: Age greater than 120 years is invalid")
  object AgeExceedsMinLimit : EditPatientValidationError("Patient Edit: Age equal to 0 years is invalid")
  object DateOfBirthExceedsMinLimit : EditPatientValidationError("Patient Edit: Age equal to 120 years is invalid")
}
