package org.simple.clinic.patient

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.newentry.DateOfBirthFormatValidator
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result
import org.simple.clinic.patient.OngoingPatientEntry.Address
import org.simple.clinic.patient.OngoingPatientEntry.PersonalDetails
import org.simple.clinic.patient.OngoingPatientEntry.PhoneNumber

@RunWith(JUnitParamsRunner::class)
class OngoingPatientEntryTest {

  @Test
  @Parameters(method = "values")
  fun `validation should fail for invalid values`(
      isValid: Boolean,
      fullname: String,
      dateOfBirth: String?,
      age: String?,
      colonyOrVillage: String,
      district: String,
      state: String,
      phoneNumber: String
  ) {
    val entry = OngoingPatientEntry(
        personalDetails = PersonalDetails(fullname, dateOfBirth, age, Gender.MALE),
        address = Address(colonyOrVillage, district, state),
        phoneNumber = PhoneNumber(phoneNumber))

    val dobValidator = DateOfBirthFormatValidator()
    assertThat(entry.validationErrors(dobValidator)).apply {
      if (isValid) {
        isEmpty()
      } else {
        isNotEmpty()
      }
    }
  }

  fun values(): Array<Any> {
    return arrayOf(
        arrayOf(false, " ", " ", " ", " ", " ", " ", " "),
        arrayOf(false, " ", null, null, " ", " ", " ", " "),
        arrayOf(false, "Ashok Kumar", "01-01-1971", "47", "colony", "state", "district", "phone-number"),
        arrayOf(true, "Ashok Kumar", "01/01/1971", null, "colony", "state", "district", "phone-number")
    )
  }

  @Test
  fun `future date-of-birth should not be accepted`() {
    val entry = OngoingPatientEntry(
        personalDetails = PersonalDetails("Ashok", "01/01/3000", "", Gender.MALE),
        address = Address("colony", "district", "state"),
        phoneNumber = PhoneNumber("phoneNumber"))

    val mockDobValidator = mock<DateOfBirthFormatValidator>()
    whenever(mockDobValidator.validate("01/01/3000")).thenReturn(Result.DATE_IS_IN_FUTURE)

    val validationErrors = entry.validationErrors(mockDobValidator)

    assertThat(validationErrors).hasSize(1)
    assertThat(validationErrors).contains(PatientEntryValidationError.DATE_OF_BIRTH_IN_FUTURE)
  }
}
