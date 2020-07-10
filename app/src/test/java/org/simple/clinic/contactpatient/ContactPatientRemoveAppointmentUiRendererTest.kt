package org.simple.clinic.contactpatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.contactpatient.RemoveAppointmentReason.AlreadyVisited
import org.simple.clinic.contactpatient.RemoveAppointmentReason.Died
import org.simple.clinic.contactpatient.RemoveAppointmentReason.MovedToPrivatePractitioner
import org.simple.clinic.contactpatient.RemoveAppointmentReason.NotResponding
import org.simple.clinic.contactpatient.RemoveAppointmentReason.OtherReason
import org.simple.clinic.contactpatient.RemoveAppointmentReason.PhoneNumberNotWorking
import org.simple.clinic.contactpatient.RemoveAppointmentReason.TransferredToAnotherFacility
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.overdue.TimeToAppointment.Days
import org.simple.clinic.overdue.TimeToAppointment.Weeks
import org.simple.clinic.util.TestUserClock
import java.time.LocalDate
import java.time.Period
import java.util.UUID

class ContactPatientRemoveAppointmentUiRendererTest {

  private val patientUuid = UUID.fromString("e6ff79b9-0ac8-4d7b-ada9-7b3056db2972")
  private val allRemoveAppointmentReasons = listOf(
      AlreadyVisited,
      NotResponding,
      PhoneNumberNotWorking,
      TransferredToAnotherFacility,
      MovedToPrivatePractitioner,
      Died,
      OtherReason
  )

  private val ui = mock<ContactPatientUi>()
  private val timeToAppointments = listOf(
      Days(1),
      Weeks(1),
      Weeks(2)
  )
  private val clock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val uiRenderer = ContactPatientUiRenderer(ui, clock)

  @Test
  fun `the list of remove appointment reasons must be rendered`() {
    // given
    val model = defaultModel()

    // when
    uiRenderer.render(model)

    // then
    verify(ui).switchToRemoveAppointmentView()
    verify(ui).renderAppointmentRemoveReasons(
        reasons = allRemoveAppointmentReasons,
        selectedReason = null
    )
    verify(ui).disableRemoveAppointmentDoneButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `the selected remove appointment reason must be rendered`() {
    // given
    val model = defaultModel().removeAppointmentReasonSelected(PhoneNumberNotWorking)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).switchToRemoveAppointmentView()
    verify(ui).renderAppointmentRemoveReasons(
        reasons = allRemoveAppointmentReasons,
        selectedReason = PhoneNumberNotWorking
    )
    verify(ui).enableRemoveAppointmentDoneButton()
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
        mode = UiMode.RemoveAppointment,
        secureCallFeatureEnabled = phoneMaskFeatureEnabled
    )
  }
}
