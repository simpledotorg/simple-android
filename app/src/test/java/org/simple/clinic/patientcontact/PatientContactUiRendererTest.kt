package org.simple.clinic.patientcontact

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.TestUserClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

class PatientContactUiRendererTest {

  private val patientUuid = UUID.fromString("e6ff79b9-0ac8-4d7b-ada9-7b3056db2972")

  private val ui = mock<PatientContactUi>()
  private val clock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val uiRenderer = PatientContactUiRenderer(ui, clock)

  @Test
  fun `when the model is not initialized, do nothing`() {
    // when
    uiRenderer.render(defaultModel())

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when the patient details are loaded with date of birth, render the patient details`() {
    // given
    val name = "Anisha Acharya"
    val dateOfBirth = LocalDate.parse("1970-01-01")
    val phoneNumber = "1234567890"
    val gender = Gender.Female

    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid,
        patientName = name,
        patientPhoneNumber = phoneNumber,
        age = null,
        dateOfBirth = dateOfBirth,
        gender = gender
    )

    // when
    uiRenderer.render(defaultModel().patientProfileLoaded(patientProfile))

    // then
    val expectedAge = 48 // difference between clock date and DOB
    verify(ui).renderPatientDetails(name, gender, expectedAge, phoneNumber)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the patient details are loaded with age, render the patient details`() {
    // given
    val name = "Anish Acharya"
    val age = Age(value = 47, updatedAt = Instant.parse("2017-01-01T00:00:00Z"))
    val phoneNumber = "1234567890"
    val gender = Gender.Male

    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid,
        patientName = name,
        patientPhoneNumber = phoneNumber,
        age = age,
        dateOfBirth = null,
        gender = gender
    )

    // when
    uiRenderer.render(defaultModel().patientProfileLoaded(patientProfile))

    // then
    val expectedAge = 48 // difference between clock date and Age
    verify(ui).renderPatientDetails(name, gender, expectedAge, phoneNumber)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `display the call result section if there is an overdue appointment`() {
    // given
    val overdueAppointment = TestData.overdueAppointment(
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid
    )

    // when
    uiRenderer.render(defaultModel().overdueAppointmentLoaded(Just(overdueAppointment)))

    // then
    verify(ui).showCallResultSection()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `hide the call result section if there is no overdue appointment`() {
    // when
    uiRenderer.render(defaultModel().overdueAppointmentLoaded(None))

    // then
    verify(ui).hideCallResultSection()
    verifyNoMoreInteractions(ui)
  }

  private fun defaultModel(
      phoneMaskFeatureEnabled: Boolean = false,
      proxyPhoneNumber: String = "12345678"
  ): PatientContactModel {
    val phoneNumberMaskerConfig = PhoneNumberMaskerConfig(proxyPhoneNumber, phoneMaskFeatureEnabled)

    return PatientContactModel.create(patientUuid, phoneNumberMaskerConfig)
  }
}
