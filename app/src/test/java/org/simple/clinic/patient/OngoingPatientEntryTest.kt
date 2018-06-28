package org.simple.clinic.patient

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

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
        personalDetails = OngoingPatientEntry.PersonalDetails(fullname, dateOfBirth, age, Gender.MALE),
        address = OngoingPatientEntry.Address(colonyOrVillage, district, state),
        phoneNumber = OngoingPatientEntry.PhoneNumber(phoneNumber))

    assertThat(entry.validationErrors()).apply {
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
}
