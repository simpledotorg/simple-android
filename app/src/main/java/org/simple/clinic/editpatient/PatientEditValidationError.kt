package org.simple.clinic.editpatient

sealed class PatientEditValidationError(val analyticsName: String) {

  object FullNameEmpty : PatientEditValidationError("Patient Edit:Name is empty")

  object PhoneNumberBlank : PatientEditValidationError("Patient Edit:Phone Number is empty")

  data class PhoneNumberTooShort(val minLength: Int) : PatientEditValidationError("Patient Edit:Phone Number is less than $minLength digits")

  data class PhoneNumberTooLong(val maxLength: Int) : PatientEditValidationError("Patient Edit:Phone Number is more than $maxLength digits")

  object ColonyOrVillageEmpty: PatientEditValidationError("Patient Edit:Colony or village empty")

  object DistrictEmpty: PatientEditValidationError("Patient Edit:District empty")

  object StateEmpty: PatientEditValidationError("Patient Edit:State empty")
}
