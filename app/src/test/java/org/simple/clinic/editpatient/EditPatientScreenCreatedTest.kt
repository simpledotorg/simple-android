package org.simple.clinic.editpatient

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.sharedTestCode.TestData
import org.simple.clinic.editpatient.EditPatientState.NOT_SAVING_PATIENT
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientAgeDetails.Type.EXACT
import org.simple.clinic.patient.PatientAgeDetails.Type.FROM_AGE
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.sharedTestCode.util.RxErrorsRule
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RunWith(JUnitParamsRunner::class)
class EditPatientScreenCreatedTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui: EditPatientUi = mock()
  private val utcClock: TestUtcClock = TestUtcClock()
  private val dateOfBirthFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private val viewRenderer = EditPatientViewRenderer(ui)

  @Test
  @Parameters(method = "params for prefilling fields on screen created")
  fun `when screen is created then the existing patient data must be prefilled`(
      patientFormTestParams: PatientFormTestParams
  ) {
    // given
    val (patient, address, phoneNumber) = patientFormTestParams
    val model = EditPatientModel
        .from(patient, address, phoneNumber, dateOfBirthFormat, null, NOT_SAVING_PATIENT, false, false)

    // when
    viewRenderer.render(model)

    // then
    verify(ui).setDistrict(address.district)
    verify(ui).setState(address.state)
    verify(ui).setGender(patient.gender)
    verify(ui).setPatientName(patient.fullName)
    verify(ui).setColonyOrVillage(address.colonyOrVillage.orEmpty())
    verify(ui).setPatientPhoneNumber(phoneNumber?.number.orEmpty())

    if (patientFormTestParams.shouldSetAge) {
      verify(ui).setPatientAge(any())
    } else {
      verify(ui, never()).setPatientAge(any())
    }

    if (patientFormTestParams.shouldSetDateOfBirth) {
      verify(ui).setPatientDateOfBirth(patient.ageDetails.dateOfBirth!!.format(dateOfBirthFormat))
    } else {
      verify(ui, never()).setPatientDateOfBirth(any())
    }
  }

  @Suppress("Unused")
  private fun `params for prefilling fields on screen created`(): List<PatientFormTestParams> {
    return listOf(
        patientFormDataWithAge(colonyOrVillage = "Colony", phoneNumber = "1111111111"),
        patientFormDataWithAge(colonyOrVillage = null, phoneNumber = "1111111111"),
        patientFormDataWithAge(colonyOrVillage = "", phoneNumber = "1111111111"),
        patientFormDataWithAge(colonyOrVillage = "Colony", phoneNumber = null),
        patientFormDataWithDateOfBirth(colonyOrVillage = "Colony", phoneNumber = null, dateOfBirth = LocalDate.parse("1995-11-28"))
    )
  }

  private fun patientFormDataWithAge(
      colonyOrVillage: String?,
      phoneNumber: String?
  ): PatientFormTestParams {
    val patientToReturn = TestData.patient(
        patientAgeDetails = PatientAgeDetails(
            ageValue = 23,
            ageUpdatedAt = Instant.now(utcClock),
            dateOfBirth = null
        )
    )
    val addressToReturn = TestData.patientAddress(uuid = patientToReturn.addressUuid, colonyOrVillage = colonyOrVillage)
    val phoneNumberToReturn = phoneNumber?.let { TestData.patientPhoneNumber(patientUuid = patientToReturn.uuid, number = it) }

    return PatientFormTestParams(
        patientToReturn,
        addressToReturn,
        phoneNumberToReturn
    )
  }

  private fun patientFormDataWithDateOfBirth(
      colonyOrVillage: String?,
      phoneNumber: String?,
      dateOfBirth: LocalDate
  ): PatientFormTestParams {
    val patientToReturn = TestData.patient(
        patientAgeDetails = PatientAgeDetails(
            dateOfBirth = dateOfBirth,
            ageValue = null,
            ageUpdatedAt = null
        )
    )
    val addressToReturn = TestData.patientAddress(uuid = patientToReturn.addressUuid, colonyOrVillage = colonyOrVillage)
    val phoneNumberToReturn = phoneNumber?.let { TestData.patientPhoneNumber(patientUuid = patientToReturn.uuid, number = it) }

    return PatientFormTestParams(
        patientToReturn,
        addressToReturn,
        phoneNumberToReturn
    )
  }

  data class PatientFormTestParams(
      val patient: Patient,
      val address: PatientAddress,
      val phoneNumber: PatientPhoneNumber?
  ) {
    val shouldSetPhoneNumber: Boolean
      get() = phoneNumber != null

    val shouldSetAge: Boolean
      get() = patient.ageDetails.type == FROM_AGE

    val shouldSetDateOfBirth: Boolean
      get() = patient.ageDetails.type == EXACT
  }
}
