package org.simple.clinic.contactpatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.overdue.TimeToAppointment.Days
import org.simple.clinic.overdue.TimeToAppointment.Weeks
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.util.TestUserClock
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
    val overdueAppointment = TestData.overdueAppointment(
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        patientAddress = TestData.overduePatientAddress(
            streetAddress = null,
            colonyOrVillage = null,
            district = "Bhatinda",
            state = "Punjab"),
        patientRegisteredFacilityName = "Bhatinda",
        diagnosedWithDiabetes = Answer.Yes,
        diagnosedWithHypertension = Answer.No
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
    verify(ui).showSecureCallUi()
    verify(ui).hideDeadPatientStatus()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if there is overdue appointment, show the call result section`() {
    // given
    val patientProfile = TestData.contactPatientProfile(patientUuid = patientUuid, generatePhoneNumber = true)
    val overdueAppointment = TestData.overdueAppointment(
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        patientAddress = TestData.overduePatientAddress(
            streetAddress = null,
            colonyOrVillage = null,
            district = "Bhatinda",
            state = "Punjab"),
        patientRegisteredFacilityName = "Bhatinda",
        diagnosedWithDiabetes = Answer.Yes,
        diagnosedWithHypertension = Answer.No
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
    verify(ui).hideDeadPatientStatus()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the secure call feature is disabled, hide the secure call ui`() {
    val patientProfile = TestData.contactPatientProfile(patientUuid = patientUuid, generatePhoneNumber = true)
    val overdueAppointment = TestData.overdueAppointment(
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        patientAddress = TestData.overduePatientAddress(
            streetAddress = null,
            colonyOrVillage = null,
            district = "Bhatinda",
            state = "Punjab"),
        patientRegisteredFacilityName = "Bhatinda",
        diagnosedWithDiabetes = Answer.Yes,
        diagnosedWithHypertension = Answer.No
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
    verify(ui).hideSecureCallUi()
    verify(ui).showPatientWithPhoneNumberCallResults()
    verify(ui).hideDeadPatientStatus()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `display patient with no phone number layout and render patient details for patient with no phone number`() {
    // given
    val patientProfile = TestData.contactPatientProfile(patientUuid = patientUuid, generatePhoneNumber = false)
    val overdueAppointment = TestData.overdueAppointment(
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        patientAddress = TestData.overduePatientAddress(
            streetAddress = null,
            colonyOrVillage = null,
            district = "Bhatinda",
            state = "Punjab"),
        patientRegisteredFacilityName = "Bhatinda",
        diagnosedWithDiabetes = Answer.Yes,
        diagnosedWithHypertension = Answer.No
    )

    // when
    uiRenderer.render(defaultModel()
        .overdueAppointmentLoaded(Optional.of(overdueAppointment))
        .contactPatientInfoLoaded()
        .contactPatientProfileLoaded(patientProfile))

    // then
    verify(ui).hideProgress()
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
    val overdueAppointment = TestData.overdueAppointment(
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        patientAddress = TestData.overduePatientAddress(
            streetAddress = null,
            colonyOrVillage = null,
            district = "Bhatinda",
            state = "Punjab"),
        patientRegisteredFacilityName = "Bhatinda",
        diagnosedWithDiabetes = Answer.Yes,
        diagnosedWithHypertension = Answer.No
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
    val overdueAppointment = TestData.overdueAppointment(
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        patientAddress = TestData.overduePatientAddress(
            streetAddress = null,
            colonyOrVillage = null,
            district = "Bhatinda",
            state = "Punjab"),
        diagnosedWithDiabetes = Answer.Yes,
        diagnosedWithHypertension = Answer.No,
        patientRegisteredFacilityID = registeredFacilityUUID
    )

    // when
    uiRenderer.render(defaultModel()
        .overdueAppointmentLoaded(Optional.of(overdueAppointment))
        .contactPatientInfoLoaded().currentFacilityLoaded(currentFacility))

    // then
    verify(ui).hideProgress()
    verify(ui).setRegisterAtLabelText()
    verify(ui).switchToCallPatientView()
    verify(ui).showPatientWithNoPhoneNumberUi()
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
    val overdueAppointment = TestData.overdueAppointment(
        facilityUuid = UUID.fromString("a607a97f-4bf6-4ce6-86a3-b266059c7734"),
        patientUuid = patientUuid,
        patientAddress = TestData.overduePatientAddress(
            streetAddress = null,
            colonyOrVillage = null,
            district = "Bhatinda",
            state = "Punjab"),
        patientRegisteredFacilityName = "Rajasthan",
        diagnosedWithDiabetes = Answer.Yes,
        diagnosedWithHypertension = Answer.No,
        patientRegisteredFacilityID = UUID.fromString("10f066d7-892a-42af-9fae-7991c3f699bb")
    )

    // when
    uiRenderer.render(defaultModel()
        .overdueAppointmentLoaded(Optional.of(overdueAppointment))
        .contactPatientInfoLoaded()
        .currentFacilityLoaded(currentFacility))

    // then
    verify(ui).hideProgress()
    verify(ui).setTransferredFromLabelText()
    verify(ui).switchToCallPatientView()
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
    verify(ui).hidePatientWithNoPhoneNumberUi()
    verify(ui).showPatientWithPhoneNumberUi()
    verify(ui).hidePatientWithPhoneNumberCallResults()
    verify(ui).showDeadPatientStatus()
    verify(ui).showSecureCallUi()
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
