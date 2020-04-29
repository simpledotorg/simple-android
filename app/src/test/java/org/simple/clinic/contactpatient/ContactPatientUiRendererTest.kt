package org.simple.clinic.contactpatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.overdue.TimeToAppointment.Days
import org.simple.clinic.overdue.TimeToAppointment.Weeks
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.TestUserClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import java.util.UUID

class ContactPatientUiRendererTest {

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
    verify(ui).hideSecureCallUi()
    verify(ui).renderSelectedAppointmentDate(timeToAppointments, Days(1), LocalDate.parse("2018-01-02"))
    verify(ui).disablePreviousReminderDateStepper()
    verify(ui).enableNextReminderDateStepper()

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
    verify(ui).hideSecureCallUi()
    verify(ui).renderSelectedAppointmentDate(timeToAppointments, Days(1), LocalDate.parse("2018-01-02"))
    verify(ui).disablePreviousReminderDateStepper()
    verify(ui).enableNextReminderDateStepper()

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
    verify(ui).hideSecureCallUi()
    verify(ui).renderSelectedAppointmentDate(timeToAppointments, Days(1), LocalDate.parse("2018-01-02"))
    verify(ui).disablePreviousReminderDateStepper()
    verify(ui).enableNextReminderDateStepper()

    verify(ui).showCallResultSection()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `hide the call result section if there is no overdue appointment`() {
    // when
    uiRenderer.render(defaultModel().overdueAppointmentLoaded(None))

    // then
    verify(ui).hideSecureCallUi()
    verify(ui).renderSelectedAppointmentDate(timeToAppointments, Days(1), LocalDate.parse("2018-01-02"))
    verify(ui).disablePreviousReminderDateStepper()
    verify(ui).enableNextReminderDateStepper()

    verify(ui).hideCallResultSection()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the secure call feature is enabled, show the secure call ui`() {
    // when
    val model = defaultModel(phoneMaskFeatureEnabled = true)
    uiRenderer.render(model)

    // then
    verify(ui).renderSelectedAppointmentDate(timeToAppointments, Days(1), LocalDate.parse("2018-01-02"))
    verify(ui).disablePreviousReminderDateStepper()
    verify(ui).enableNextReminderDateStepper()

    verify(ui).showSecureCallUi()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the secure call feature is disabled, hide the secure call ui`() {
    // when
    val model = defaultModel(phoneMaskFeatureEnabled = false)
    uiRenderer.render(model)

    // then
    verify(ui).renderSelectedAppointmentDate(timeToAppointments, Days(1), LocalDate.parse("2018-01-02"))
    verify(ui).disablePreviousReminderDateStepper()
    verify(ui).enableNextReminderDateStepper()

    verify(ui).hideSecureCallUi()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `the current selected appointment date must be shown long with the reminder periods`() {
    // given
    val reminderPeriod = Weeks(1)
    val selectedReminderDate = PotentialAppointmentDate(
        timeToAppointment = reminderPeriod,
        scheduledFor = LocalDate.parse("2018-01-08")
    )

    // when
    val model = defaultModel().reminderDateSelected(selectedReminderDate)
    uiRenderer.render(model)

    // then
    verify(ui).hideSecureCallUi()
    verify(ui).enablePreviousReminderDateStepper()
    verify(ui).enableNextReminderDateStepper()

    verify(ui).renderSelectedAppointmentDate(timeToAppointments, reminderPeriod, selectedReminderDate.scheduledFor)
    verifyNoMoreInteractions(ui)
  }
  
  @Test
  fun `when the current selected date is the earliest available date, disable the previous date button`() {
    // given
    val reminderPeriod = Days(1)
    val selectedReminderDate = PotentialAppointmentDate(
        timeToAppointment = reminderPeriod,
        scheduledFor = LocalDate.parse("2018-01-02")
    )

    // when
    val model = defaultModel().reminderDateSelected(selectedReminderDate)
    uiRenderer.render(model)

    // then
    verify(ui).hideSecureCallUi()
    verify(ui).renderSelectedAppointmentDate(timeToAppointments, reminderPeriod, selectedReminderDate.scheduledFor)
    verify(ui).enableNextReminderDateStepper()

    verify(ui).disablePreviousReminderDateStepper()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the current selected date is not the earliest available date, enable the previous date button`() {
    // given
    val reminderPeriod = Weeks(1)
    val selectedReminderDate = PotentialAppointmentDate(
        timeToAppointment = reminderPeriod,
        scheduledFor = LocalDate.parse("2018-01-08")
    )

    // when
    val model = defaultModel().reminderDateSelected(selectedReminderDate)
    uiRenderer.render(model)

    // then
    verify(ui).hideSecureCallUi()
    verify(ui).renderSelectedAppointmentDate(timeToAppointments, reminderPeriod, selectedReminderDate.scheduledFor)
    verify(ui).enableNextReminderDateStepper()

    verify(ui).enablePreviousReminderDateStepper()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the current selected date is the latest available date, disable the next date button`() {
    // given
    val reminderPeriod = Weeks(2)
    val selectedReminderDate = PotentialAppointmentDate(
        timeToAppointment = reminderPeriod,
        scheduledFor = LocalDate.parse("2018-01-15")
    )

    // when
    val model = defaultModel().reminderDateSelected(selectedReminderDate)
    uiRenderer.render(model)

    // then
    verify(ui).hideSecureCallUi()
    verify(ui).renderSelectedAppointmentDate(timeToAppointments, reminderPeriod, selectedReminderDate.scheduledFor)
    verify(ui).enablePreviousReminderDateStepper()

    verify(ui).disableNextReminderDateStepper()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the current selected date is not the latest available date, enable the next date button`() {
    // given
    val reminderPeriod = Weeks(1)
    val selectedReminderDate = PotentialAppointmentDate(
        timeToAppointment = reminderPeriod,
        scheduledFor = LocalDate.parse("2018-01-08")
    )

    // when
    val model = defaultModel().reminderDateSelected(selectedReminderDate)
    uiRenderer.render(model)

    // then
    verify(ui).hideSecureCallUi()
    verify(ui).renderSelectedAppointmentDate(timeToAppointments, reminderPeriod, selectedReminderDate.scheduledFor)
    verify(ui).enablePreviousReminderDateStepper()

    verify(ui).enableNextReminderDateStepper()
    verifyNoMoreInteractions(ui)
  }

  private fun defaultModel(
      phoneMaskFeatureEnabled: Boolean = false,
      proxyPhoneNumber: String = "12345678",
      timeToAppointments: List<TimeToAppointment> = this.timeToAppointments,
      mode: UiMode = UiMode.CallPatient
  ): ContactPatientModel {
    val phoneNumberMaskerConfig = PhoneNumberMaskerConfig(proxyPhoneNumber, phoneMaskFeatureEnabled)
    val appointmentConfig = AppointmentConfig(
        appointmentDuePeriodForDefaulters = Period.ZERO,
        scheduleAppointmentsIn = emptyList(),
        defaultTimeToAppointment = Days(0),
        periodForIncludingOverdueAppointments = Period.ZERO,
        remindAppointmentsIn = timeToAppointments
    )

    return ContactPatientModel.create(
        patientUuid = patientUuid,
        phoneNumberMaskerConfig = phoneNumberMaskerConfig,
        appointmentConfig = appointmentConfig,
        userClock = clock,
        mode = mode
    )
  }
}
