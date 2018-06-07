package org.resolvetosavelives.red.patient

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.resolvetosavelives.red.patient.OngoingPatientEntry.ValidationResult

@RunWith(JUnitParamsRunner::class)
class OngoingPatientEntryTest {

  @Test
  @Parameters(method = "values")
  fun `validation should fail for invalid values`(
      expectedResultClass: Class<out ValidationResult>,
      fullname: String,
      dateOfBirth: String?,
      age: String?,
      colonyOrVillage: String,
      district: String,
      state: String,
      phoneNumber: String
  ) {
    val entry = OngoingPatientEntry(
        personalDetails = OngoingPatientEntry.PersonalDetails(fullname, dateOfBirth, age, Gender.MALE),
        address = OngoingPatientEntry.Address(colonyOrVillage, district, state),
        phoneNumber = OngoingPatientEntry.PhoneNumber(phoneNumber))

    val validationResult = entry.validateForSaving()
    assertThat(validationResult).isInstanceOf(expectedResultClass)
  }

  fun values(): Array<Any> {
    return arrayOf(
        arrayOf(ValidationResult.Invalid::class.java, " ", " ", " ", " ", " ", " ", " "),
        arrayOf(ValidationResult.Invalid::class.java, " ", null, null, " ", " ", " ", " "),
        arrayOf(ValidationResult.Invalid::class.java, "Ashok Kumar", "01-01-1971", "47", "colony", "state", "district", "phone-number"),
        arrayOf(ValidationResult.Valid::class.java, "Ashok Kumar", "01-01-1971", null, "colony", "state", "district", "phone-number")
    )
  }
}
