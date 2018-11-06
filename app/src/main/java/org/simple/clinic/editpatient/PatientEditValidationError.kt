package org.simple.clinic.editpatient

sealed class PatientEditValidationError(val analyticsName: String) {

  object FullNameEmpty : PatientEditValidationError("Edit Patient:Name is empty")

  object PhoneNumberBlank : PatientEditValidationError("Patient Entry:Phone Number is empty")

  data class PhoneNumberTooShort(val minLength: Int) : PatientEditValidationError("Patient Entry:Phone Number is less than $minLength digits")

  data class PhoneNumberTooLong(val maxLength: Int) : PatientEditValidationError("Patient Entry:Phone Number is more than $maxLength digits")

  object ColonyOrVillageEmpty: PatientEditValidationError("Patient Entry:Colony or village empty")

  object DistrictEmpty: PatientEditValidationError("Patient Entry:District empty")

  object StateEmpty: PatientEditValidationError("Patient Entry:State empty")
}
