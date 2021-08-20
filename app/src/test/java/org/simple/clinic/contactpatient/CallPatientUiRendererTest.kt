package org.simple.clinic.contactpatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.overdue.TimeToAppointment.Days
import org.simple.clinic.overdue.TimeToAppointment.Weeks
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAddress
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
  fun `when the patient details are loaded with date of birth, render the patient details`() {
    // given
    val name = "Anisha Acharya"
    val dateOfBirth = LocalDate.parse("1970-01-01")
    val phoneNumber = "1234567890"
    val gender = Gender.Female

    val patientProfile = TestData.contactPatientProfile(
        patientUuid = patientUuid,
        patientName = name,
        patientPhoneNumber = phoneNumber,
        age = null,
        dateOfBirth = dateOfBirth,
        gender = gender
    )

    // when
    uiRenderer.render(defaultModel().contactPatientProfileLoaded(patientProfile)
        .contactPatientInfoLoaded())

    // then
    verify(ui).hideProgress()
    verify(ui).hideSecureCallUi_Old()
    verify(ui).switchToCallPatientView_Old()

    val expectedAge = 48 // difference between clock date and DOB
    verify(ui).renderPatientDetails_Old(name, gender, expectedAge, phoneNumber)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the patient details are loaded with age, render the patient details`() {
    // given
    val name = "Anish Acharya"
    val age = Age(value = 47, updatedAt = Instant.parse("2017-01-01T00:00:00Z"))
    val phoneNumber = "1234567890"
    val gender = Gender.Male

    val patientProfile = TestData.contactPatientProfile(
        patientUuid = patientUuid,
        patientName = name,
        patientPhoneNumber = phoneNumber,
        age = age,
        dateOfBirth = null,
        gender = gender
    )

    // when
    uiRenderer.render(defaultModel().contactPatientProfileLoaded(patientProfile)
        .contactPatientInfoLoaded())

    // then
    verify(ui).hideProgress()
    verify(ui).hideSecureCallUi_Old()
    verify(ui).switchToCallPatientView_Old()

    val expectedAge = 48 // difference between clock date and Age
    verify(ui).renderPatientDetails_Old(name, gender, expectedAge, phoneNumber)
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
    uiRenderer.render(defaultModel().overdueAppointmentLoaded(Optional.of(overdueAppointment))
        .contactPatientInfoLoaded())

    // then
    verify(ui).hideProgress()
    verify(ui).hideSecureCallUi_Old()
    verify(ui).switchToCallPatientView_Old()

    verify(ui).showCallResultSection_Old()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `hide the call result section if there is no overdue appointment`() {
    // when
    uiRenderer.render(defaultModel().overdueAppointmentLoaded(Optional.empty())
        .contactPatientInfoLoaded())

    // then
    verify(ui).hideProgress()
    verify(ui).hideSecureCallUi_Old()
    verify(ui).switchToCallPatientView_Old()

    verify(ui).hideCallResultSection_Old()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the secure call feature is enabled, show the old secure call ui`() {
    // when
    val model = defaultModel(phoneMaskFeatureEnabled = true)
        .contactPatientInfoLoaded()
    uiRenderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).switchToCallPatientView_Old()

    verify(ui).showSecureCallUi_Old()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the secure call feature is enabled and overdue list changes is enabled, show the secure call ui`() {
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
    val model = defaultModel(phoneMaskFeatureEnabled = true, overdueListChangesFeatureEnabled = true)
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
    verify(ui).showPatientWithCallResultUi()
    verify(ui).showPatientWithPhoneNumberUi()
    verify(ui).hidePatientWithNoPhoneNumberUi()
    verify(ui).setResultOfCallLabelText()
    verify(ui).showSecureCallUi()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if overdue list changes is enabled and there is overdue appointment, show the call result section`() {
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
    val model = defaultModel(phoneMaskFeatureEnabled = true, overdueListChangesFeatureEnabled = true)
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
    verify(ui).showPatientWithCallResultUi()
    verify(ui).setResultOfCallLabelText()
    verify(ui).showSecureCallUi()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the secure call feature is disabled and overdue list feature is disabled, hide the old secure call ui`() {
    // when
    val model = defaultModel(phoneMaskFeatureEnabled = false)
        .contactPatientInfoLoaded()
    uiRenderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).switchToCallPatientView_Old()

    verify(ui).hideSecureCallUi_Old()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the secure call feature is disabled and overdue list feature is enabled, hide the secure call ui`() {
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
    val model = defaultModel(phoneMaskFeatureEnabled = false, overdueListChangesFeatureEnabled = true)
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
    verify(ui).showPatientWithCallResultUi()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the overdue list changes feature is enabled, then switch to call patient view`() {
    // when
    val model = defaultModel(phoneMaskFeatureEnabled = false, overdueListChangesFeatureEnabled = true)
        .contactPatientInfoLoaded()
    uiRenderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).switchToCallPatientView()
    verify(ui).showPatientWithNoPhoneNumberUi()
    verify(ui).hidePatientWithPhoneNumberUi()
    verify(ui).setResultLabelText()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the overdue list changes feature is disabled, then switch to old call patient view`() {
    // when
    val model = defaultModel(phoneMaskFeatureEnabled = false, overdueListChangesFeatureEnabled = false)
        .contactPatientInfoLoaded()
    uiRenderer.render(model)

    // then
    verify(ui).hideProgress()
    verify(ui).switchToCallPatientView_Old()
    verify(ui).hideSecureCallUi_Old()
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
    uiRenderer.render(defaultModel(overdueListChangesFeatureEnabled = true)
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
    uiRenderer.render(defaultModel(overdueListChangesFeatureEnabled = true, phoneMaskFeatureEnabled = true)
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
    verify(ui).showPatientWithCallResultUi()
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
    uiRenderer.render(defaultModel(overdueListChangesFeatureEnabled = true)
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
    uiRenderer.render(defaultModel(overdueListChangesFeatureEnabled = true)
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
    verifyNoMoreInteractions(ui)
  }

  private fun defaultModel(
      phoneMaskFeatureEnabled: Boolean = false,
      timeToAppointments: List<TimeToAppointment> = this.timeToAppointments,
      overdueListChangesFeatureEnabled: Boolean = false,
      currentFacility: Facility? = null
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
        secureCallFeatureEnabled = phoneMaskFeatureEnabled,
        overdueListChangesFeatureEnabled = overdueListChangesFeatureEnabled
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
