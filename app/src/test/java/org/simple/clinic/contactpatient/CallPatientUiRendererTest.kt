package org.simple.clinic.contactpatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.overdue.TimeToAppointment.Days
import org.simple.clinic.overdue.TimeToAppointment.Weeks
import org.simple.clinic.overdue.callresult.Outcome
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.util.TestUserClock
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.util.Optional
import java.util.UUID

class CallPatientUiRendererTest {

  private val patientUuid = UUID.fromString("e6ff79b9-0ac8-4d7b-ada9-7b3056db2972")

  private val ui = mock<ContactPatientUi>()
  private val timeToAppointments = listOf(
      Days(1),
      Weeks(1),
      Weeks(2)
  )
  private val clock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val uiRenderer = ContactPatientUiRenderer(ui, clock)

  @Test
  fun `when contact patient information is loading, then show progress`() {
    // when
    uiRenderer.render(defaultModel())

    // then
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the secure call feature is enabled, show the secure call ui`() {
    // given
    val patientProfile = TestData.contactPatientProfile(patientUuid = patientUuid, generatePhoneNumber = true)
    val overdueAppointment = TestData.appointment(
        uuid = UUID.fromString("60825c79-5588-4db0-9a57-ee6178f57f0d"),
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        status = Scheduled
    )

    // when
    val model = defaultModel(phoneMaskFeatureEnabled = true)
        .contactPatientInfoLoaded().contactPatientProfileLoaded(patientProfile).overdueAppointmentLoaded(Optional.of(overdueAppointment))
    uiRenderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).switchToCallPatientView()
    verify(ui).renderPatientDetails(PatientDetails(name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientProfile.patient.ageDetails.estimateAge(clock),
        phoneNumber = patientProfile.phoneNumbers.first().number,
        patientAddress = patientAddressText(patientProfile.address)!!,
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen))
    verify(ui).showPatientWithPhoneNumberCallResults()
    verify(ui).showPatientWithPhoneNumberUi()
    verify(ui).hidePatientWithNoPhoneNumberUi()
    verify(ui).setResultOfCallLabelText()
    verify(ui).hideCallResult()
    verify(ui).showSecureCallUi()
    verify(ui).hideDeadPatientStatus()
    verify(ui).showNormalCallButtonText()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if there is overdue appointment, show the call result section`() {
    // given
    val patientProfile = TestData.contactPatientProfile(patientUuid = patientUuid, generatePhoneNumber = true)
    val overdueAppointment = TestData.appointment(
        uuid = UUID.fromString("fc2da02d-bb8b-4b39-a011-2a7365b43d39"),
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        status = Scheduled
    )

    // when
    val model = defaultModel(phoneMaskFeatureEnabled = true)
        .contactPatientInfoLoaded()
        .contactPatientProfileLoaded(patientProfile)
        .overdueAppointmentLoaded(Optional.of(overdueAppointment))
    uiRenderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).switchToCallPatientView()
    verify(ui).renderPatientDetails(PatientDetails(name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientProfile.patient.ageDetails.estimateAge(clock),
        phoneNumber = patientProfile.phoneNumbers.first().number,
        patientAddress = patientAddressText(patientProfile.address)!!,
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen))
    verify(ui).hidePatientWithNoPhoneNumberUi()
    verify(ui).showPatientWithPhoneNumberUi()
    verify(ui).showPatientWithPhoneNumberCallResults()
    verify(ui).setResultOfCallLabelText()
    verify(ui).showSecureCallUi()
    verify(ui).hideCallResult()
    verify(ui).showNormalCallButtonText()
    verify(ui).hideDeadPatientStatus()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the secure call feature is disabled, hide the secure call ui`() {
    val patientProfile = TestData.contactPatientProfile(patientUuid = patientUuid, generatePhoneNumber = true)
    val overdueAppointment = TestData.appointment(
        uuid = UUID.fromString("59ab2060-0a3c-4908-bff4-8d0f4916a545"),
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        status = Scheduled
    )

    // when
    val model = defaultModel(phoneMaskFeatureEnabled = false)
        .contactPatientInfoLoaded().contactPatientProfileLoaded(patientProfile).overdueAppointmentLoaded(Optional.of(overdueAppointment))

    uiRenderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).switchToCallPatientView()
    verify(ui).renderPatientDetails(PatientDetails(name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientProfile.patient.ageDetails.estimateAge(clock),
        phoneNumber = patientProfile.phoneNumbers.first().number,
        patientAddress = patientAddressText(patientProfile.address)!!,
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen))
    verify(ui).showPatientWithPhoneNumberUi()
    verify(ui).hidePatientWithNoPhoneNumberUi()
    verify(ui).setResultOfCallLabelText()
    verify(ui).hideCallResult()
    verify(ui).hideSecureCallUi()
    verify(ui).showCallButtonText()
    verify(ui).showPatientWithPhoneNumberCallResults()
    verify(ui).hideDeadPatientStatus()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `display patient with no phone number layout and render patient details for patient with no phone number`() {
    // given
    val patientProfile = TestData.contactPatientProfile(patientUuid = patientUuid, generatePhoneNumber = false)
    val overdueAppointment = TestData.appointment(
        uuid = UUID.fromString("7dd074a9-6493-4cd4-a4af-f4e4b2edd678"),
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        status = Scheduled
    )

    // when
    uiRenderer.render(defaultModel()
        .overdueAppointmentLoaded(Optional.of(overdueAppointment))
        .contactPatientInfoLoaded()
        .contactPatientProfileLoaded(patientProfile))

    // then
    verify(ui).hideProgress()
    verify(ui).hideCallResult()
    verify(ui).showPatientWithNoPhoneNumberUi()
    verify(ui).hidePatientWithPhoneNumberUi()
    verify(ui).renderPatientDetails(PatientDetails(name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientProfile.patient.ageDetails.estimateAge(clock),
        phoneNumber = null,
        patientAddress = patientAddressText(patientProfile.address)!!,
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen))
    verify(ui).switchToCallPatientView()
    verify(ui).setResultLabelText()
    verify(ui).showPatientWithNoPhoneNumberResults()
    verify(ui).hideDeadPatientStatus()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `display patient with phone number layout and render patient details for patient with phone number`() {
    // given
    val patientProfile = TestData.contactPatientProfile(patientUuid = patientUuid, generatePhoneNumber = true)
    val overdueAppointment = TestData.appointment(
        uuid = UUID.fromString("8fc0339e-5f03-430e-8ff4-dfeb0f78c24e"),
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        status = Scheduled
    )

    // when
    uiRenderer.render(defaultModel(phoneMaskFeatureEnabled = true)
        .overdueAppointmentLoaded(Optional.of(overdueAppointment))
        .contactPatientInfoLoaded()
        .contactPatientProfileLoaded(patientProfile))

    // then
    verify(ui).hideProgress()
    verify(ui).showPatientWithPhoneNumberUi()
    verify(ui).hidePatientWithNoPhoneNumberUi()
    verify(ui).renderPatientDetails(PatientDetails(name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientProfile.patient.ageDetails.estimateAge(clock),
        phoneNumber = patientProfile.phoneNumbers.first().number,
        patientAddress = patientAddressText(patientProfile.address)!!,
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen))
    verify(ui).showSecureCallUi()
    verify(ui).hideCallResult()
    verify(ui).showNormalCallButtonText()
    verify(ui).switchToCallPatientView()
    verify(ui).setResultOfCallLabelText()
    verify(ui).showPatientWithPhoneNumberCallResults()
    verify(ui).hideDeadPatientStatus()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `display registered at facility label text when patient's registered facility is the same as user's current facility`() {
    // given
    val registeredFacilityUUID = UUID.fromString("1749461e-0ff7-47d9-95e0-fa4337d118b3")
    val currentFacility = TestData.facility(uuid = registeredFacilityUUID)
    val patientProfile = TestData.contactPatientProfile(
        patientUuid = patientUuid,
        patientStatus = PatientStatus.Active,
        generatePhoneNumber = false,
        patientRegisteredFacilityId = registeredFacilityUUID
    )

    val overdueAppointment = TestData.appointment(
        uuid = UUID.fromString("dbcc8469-9cab-4c61-8df8-bb6678504e6e"),
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        status = Scheduled
    )

    // when
    uiRenderer.render(defaultModel()
        .contactPatientProfileLoaded(patientProfile)
        .overdueAppointmentLoaded(Optional.of(overdueAppointment))
        .contactPatientInfoLoaded().currentFacilityLoaded(currentFacility))

    // then
    verify(ui).hideProgress()
    verify(ui).renderPatientDetails(PatientDetails(name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientProfile.patient.ageDetails.estimateAge(clock),
        phoneNumber = null,
        patientAddress = patientAddressText(patientProfile.address)!!,
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen))
    verify(ui).setRegisterAtLabelText()
    verify(ui).switchToCallPatientView()
    verify(ui).showPatientWithNoPhoneNumberUi()
    verify(ui).hideCallResult()
    verify(ui).hidePatientWithPhoneNumberUi()
    verify(ui).setResultLabelText()
    verify(ui).showPatientWithNoPhoneNumberResults()
    verify(ui).hideDeadPatientStatus()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `display transferred from facility label text when patient's registered facility is not the same as user's current facility`() {
    // given
    val currentFacility = TestData.facility(uuid = UUID.fromString("1749461e-0ff7-47d9-95e0-fa4337d118b3"), name = "Bhatinda")
    val patientProfile = TestData.contactPatientProfile(
        patientUuid = patientUuid,
        patientStatus = PatientStatus.Dead,
        generatePhoneNumber = false,
        patientRegisteredFacilityId = UUID.fromString("62a3b371-519b-4f59-8dee-5270261a372d")
    )
    val overdueAppointment = TestData.appointment(
        uuid = UUID.fromString("a98fa374-1a17-400d-9723-6c2f87a9d439"),
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        status = Scheduled
    )

    // when
    uiRenderer.render(defaultModel()
        .contactPatientProfileLoaded(patientProfile)
        .overdueAppointmentLoaded(Optional.of(overdueAppointment))
        .contactPatientInfoLoaded()
        .currentFacilityLoaded(currentFacility))

    // then
    verify(ui).hideProgress()
    verify(ui).renderPatientDetails(PatientDetails(name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientProfile.patient.ageDetails.estimateAge(clock),
        phoneNumber = null,
        patientAddress = patientAddressText(patientProfile.address)!!,
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen))
    verify(ui).setTransferredFromLabelText()
    verify(ui).switchToCallPatientView()
    verify(ui).hideCallResult()
    verify(ui).showPatientWithNoPhoneNumberUi()
    verify(ui).hidePatientWithPhoneNumberUi()
    verify(ui).setResultLabelText()
    verify(ui).showPatientWithNoPhoneNumberResults()
    verify(ui).hideDeadPatientStatus()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient is dead and has phone number, then show patient died status`() {
    // given
    val currentFacility = TestData.facility(
        uuid = UUID.fromString("1749461e-0ff7-47d9-95e0-fa4337d118b3"),
        name = "Bhatinda"
    )
    val patientProfile = TestData.contactPatientProfile(
        patientUuid = patientUuid,
        patientStatus = PatientStatus.Dead,
        patientPhoneNumber = "1234567890",
        generatePhoneNumber = false
    )

    // when
    val defaultModel = defaultModel(
        phoneMaskFeatureEnabled = true
    )
    uiRenderer.render(
        defaultModel
            .currentFacilityLoaded(currentFacility)
            .contactPatientProfileLoaded(patientProfile)
            .contactPatientInfoLoaded()
    )

    // then
    verify(ui).hideProgress()
    verify(ui).renderPatientDetails(PatientDetails(name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientProfile.patient.ageDetails.estimateAge(clock),
        phoneNumber = patientProfile.phoneNumbers.first().number,
        patientAddress = patientAddressText(patientProfile.address)!!,
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen))
    verify(ui).switchToCallPatientView()
    verify(ui).hideCallResult()
    verify(ui).hidePatientWithNoPhoneNumberUi()
    verify(ui).showPatientWithPhoneNumberUi()
    verify(ui).hidePatientWithPhoneNumberCallResults()
    verify(ui).showDeadPatientStatus()
    verify(ui).showSecureCallUi()
    verify(ui).showNormalCallButtonText()
    verify(ui).setTransferredFromLabelText()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when call result for appointment is present, render call result for appointment message in the ui`() {
    // given
    val appointmentId = UUID.fromString("c3b8c0f9-567e-45a5-b4aa-98f06d01aaa5")
    val callResultOutcome = Outcome.RemindToCallLater
    val updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    val callResult = TestData.callResult(
        id = UUID.fromString("0135537e-f6a0-46d0-8e4d-009f938889bc"),
        appointmentId = appointmentId,
        outcome = callResultOutcome,
        updatedAt = updatedAt)
    val currentFacility = TestData.facility(
        uuid = UUID.fromString("1749461e-0ff7-47d9-95e0-fa4337d118b3"),
        name = "Bhatinda"
    )
    val patientProfile = TestData.contactPatientProfile(
        patientUuid = patientUuid,
        patientStatus = PatientStatus.Dead,
        patientPhoneNumber = "1234567890",
        generatePhoneNumber = false
    )

    // when
    uiRenderer.render(defaultModel(phoneMaskFeatureEnabled = true)
        .contactPatientProfileLoaded(patientProfile)
        .contactPatientInfoLoaded()
        .callResultLoaded(Optional.of(callResult)))

    // then
    verify(ui).hideProgress()
    verify(ui).showCallResult()
    verify(ui).renderPatientDetails(PatientDetails(name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientProfile.patient.ageDetails.estimateAge(clock),
        phoneNumber = patientProfile.phoneNumbers.first().number,
        patientAddress = patientAddressText(patientProfile.address)!!,
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen))
    verify(ui).switchToCallPatientView()
    verify(ui).hidePatientWithNoPhoneNumberUi()
    verify(ui).showPatientWithPhoneNumberUi()
    verify(ui).hidePatientWithPhoneNumberCallResults()
    verify(ui).showDeadPatientStatus()
    verify(ui).showSecureCallUi()
    verify(ui).showNormalCallButtonText()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when call result for appointment is absent, hide call result for appointment message in the ui`() {
    // given
    val updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    val currentFacility = TestData.facility(
        uuid = UUID.fromString("1749461e-0ff7-47d9-95e0-fa4337d118b3"),
        name = "Bhatinda"
    )
    val patientProfile = TestData.contactPatientProfile(
        patientUuid = patientUuid,
        patientStatus = PatientStatus.Dead,
        patientPhoneNumber = "1234567890",
        generatePhoneNumber = false
    )

    // when
    uiRenderer.render(defaultModel(phoneMaskFeatureEnabled = true)
        .contactPatientProfileLoaded(patientProfile)
        .contactPatientInfoLoaded()
    )

    // then
    verify(ui).hideProgress()
    verify(ui).hideCallResult()
    verify(ui).renderPatientDetails(PatientDetails(name = patientProfile.patient.fullName,
        gender = patientProfile.patient.gender,
        age = patientProfile.patient.ageDetails.estimateAge(clock),
        phoneNumber = patientProfile.phoneNumbers.first().number,
        patientAddress = patientAddressText(patientProfile.address)!!,
        registeredFacility = patientProfile.registeredFacility?.name,
        diagnosedWithDiabetes = patientProfile.medicalHistory?.diagnosedWithDiabetes,
        diagnosedWithHypertension = patientProfile.medicalHistory?.diagnosedWithHypertension,
        lastVisited = patientProfile.patientLastSeen))
    verify(ui).switchToCallPatientView()
    verify(ui).hidePatientWithNoPhoneNumberUi()
    verify(ui).showPatientWithPhoneNumberUi()
    verify(ui).hidePatientWithPhoneNumberCallResults()
    verify(ui).showDeadPatientStatus()
    verify(ui).showSecureCallUi()
    verify(ui).showNormalCallButtonText()
    verifyNoMoreInteractions(ui)
  }

  private fun defaultModel(
      phoneMaskFeatureEnabled: Boolean = false,
      timeToAppointments: List<TimeToAppointment> = this.timeToAppointments
  ): ContactPatientModel {
    val appointmentConfig = AppointmentConfig(
        appointmentDuePeriodForDefaulters = Period.ZERO,
        scheduleAppointmentsIn = emptyList(),
        defaultTimeToAppointment = Days(0),
        periodForIncludingOverdueAppointments = Period.ZERO,
        remindAppointmentsIn = timeToAppointments
    )

    return ContactPatientModel.create(
        patientUuid = patientUuid,
        appointmentConfig = appointmentConfig,
        userClock = clock,
        mode = UiMode.CallPatient,
        secureCallFeatureEnabled = phoneMaskFeatureEnabled
    )
  }

  private fun patientAddressText(patientAddress: PatientAddress) = when {
    !patientAddress.streetAddress.isNullOrBlank() && !patientAddress.colonyOrVillage.isNullOrBlank() -> {
      "${patientAddress.streetAddress}, ${patientAddress.colonyOrVillage}"
    }
    !patientAddress.streetAddress.isNullOrBlank() -> patientAddress.streetAddress
    !patientAddress.colonyOrVillage.isNullOrBlank() -> patientAddress.colonyOrVillage
    else -> "${patientAddress.district}, ${patientAddress.state}"
  }
}
