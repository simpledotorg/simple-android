package org.simple.clinic.scheduleappointment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.newentry.ButtonState
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRequestInfo
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultStatus
import org.simple.clinic.util.TestUserClock
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.util.UUID
import org.simple.clinic.scheduleappointment.ButtonState as NextButtonState

class ScheduleAppointmentUiRendererTest {
  private val ui = mock<ScheduleAppointmentUi>()
  private val uiRendererTest = ScheduleAppointmentUiRenderer(ui = ui)
  private val patientUuid = UUID.fromString("f2f8c144-9859-4a26-9d7e-3bfe5f86c199")
  private val requesterId = UUID.fromString("824ef2ed-3d81-47fc-a4b3-b44bd80a9757")
  private val facilityId = UUID.fromString("b434ba30-4dfa-47dd-a184-25957c5cc1fa")
  private val teleconsultRecordUuid = UUID.fromString("b9e45df8-51c0-4bab-bc6d-c34e6084710f")
  val appointmentConfig: AppointmentConfig = AppointmentConfig(
      appointmentDuePeriodForDefaulters = Period.ofDays(30),
      scheduleAppointmentsIn = listOf(TimeToAppointment.Days(3)),
      defaultTimeToAppointment = TimeToAppointment.Days(5),
      periodForIncludingOverdueAppointments = Period.ofMonths(12),
      remindAppointmentsIn = emptyList())
  val clock = TestUserClock(LocalDate.parse("2020-10-26"))


  val defaultModel = ScheduleAppointmentModel.create(
      patientUuid = patientUuid,
      timeToAppointments = appointmentConfig.scheduleAppointmentsIn,
      userClock = clock,
      doneButtonState = ButtonState.SAVED,
      nextButtonState = NextButtonState.SCHEDULED
  )

  @Test
  fun `when teleconsult record is not present, then show save button`() {
    // given
    defaultModel.teleconsultRecordLoaded(null)

    // when
    uiRendererTest.render(defaultModel)

    // then
    verify(ui).showDoneButton()
    verify(ui).hideNextButton()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when teleconsult record is present and teleconsult request status is yes or no then show save button`() {
    // given
    val teleconsultRequestInfo = TeleconsultRequestInfo(
        requesterId = requesterId,
        facilityId = facilityId,
        requestedAt = Instant.now(),
        requesterCompletionStatus = TeleconsultStatus.Yes
    )
    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordUuid,
        teleconsultRequestInfo = teleconsultRequestInfo
    )

    defaultModel.teleconsultRecordLoaded(teleconsultRecord = teleconsultRecord)

    // when
    uiRendererTest.render(defaultModel)

    // then
    verify(ui).showDoneButton()
    verify(ui).hideNextButton()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when teleconsult record is present and teleconsult request status is still waiting or null then show next button`() {
    // given
    val teleconsultRequestInfo = TeleconsultRequestInfo(
        requesterId = requesterId,
        facilityId = facilityId,
        requestedAt = Instant.now(),
        requesterCompletionStatus = TeleconsultStatus.StillWaiting
    )
    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordUuid,
        teleconsultRequestInfo = teleconsultRequestInfo
    )

    val model = defaultModel.teleconsultRecordLoaded(teleconsultRecord = teleconsultRecord)
    // when
    uiRendererTest.render(model)

    // then
    verify(ui).showNextButton()
    verify(ui).hideDoneButton()
    verify(ui).hideProgress()
    verifyNoMoreInteractions(ui)
  }
}
