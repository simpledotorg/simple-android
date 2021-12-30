package org.simple.clinic.summary.nextappointment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class NextAppointmentUiRendererTest {

  private val ui = mock<NextAppointmentUi>()
  private val uiRenderer = NextAppointmentUiRenderer(ui = ui)
  private val patientUuid = UUID.fromString("305c6c33-90b5-4895-9f82-e6d071d05955")
  private val defaultModel = NextAppointmentModel.default(patientUuid = patientUuid)

  @Test
  fun `when appointment is not present, then render no appointment view`() {
    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).showNoAppointment()
    verify(ui).showAddAppointmentButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when appointment is present, then show appointment date`() {
    // given
    val appointment = TestData.appointment(
        uuid = UUID.fromString("a1a69a69-299b-483c-8dc9-5ef8cf9b1433"),
        patientUuid = patientUuid,
        scheduledDate = LocalDate.parse("2018-02-01"),
        status = Scheduled,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null,
        facilityUuid = UUID.fromString("2b5ab9f7-6b6c-463d-9a4c-a43b25ccf517"),
        creationFacilityUuid = UUID.fromString("2b5ab9f7-6b6c-463d-9a4c-a43b25ccf517")
    )
    val appointmentLoadedModel = defaultModel.appointmentLoaded(appointment)

    // when
    uiRenderer.render(appointmentLoadedModel)

    // then
    verify(ui).showChangeAppointmentButton()
    verify(ui).showAppointmentDate(LocalDate.parse("2018-02-01"))
    verifyNoMoreInteractions(ui)
  }
}
