package org.simple.clinic.summary.nextappointment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.overdue.Appointment.Status.Scheduled
import org.simple.clinic.patient.PatientAndAssignedFacility
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

    val assignedFacility = TestData.facility(
        uuid = UUID.fromString("2b5ab9f7-6b6c-463d-9a4c-a43b25ccf517"),
        name = "PHC Obvious"
    )

    val patient = TestData.patient(
        uuid = UUID.fromString("da7004ef-d144-42cb-9179-e21e46612842"),
        fullName = "Ramesh Mehta",
        assignedFacilityId = assignedFacility.uuid
    )

    val patientAndAssignedFacility = PatientAndAssignedFacility(patient, assignedFacility)

    val model = defaultModel
        .appointmentLoaded(appointment)
        .patientAndAssignedFacilityLoaded(patientAndAssignedFacility)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showChangeAppointmentButton()
    verify(ui).showAppointmentDate(LocalDate.parse("2018-02-01"))
    verify(ui).hideAssignedFacility()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when appointment facility is not same as patient assigned facility, then show assigned facility name`() {
    // given
    val appointment = TestData.appointment(
        uuid = UUID.fromString("eb1fd5c6-502c-413b-83e7-114213bb0091"),
        patientUuid = patientUuid,
        scheduledDate = LocalDate.parse("2018-02-01"),
        status = Scheduled,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        deletedAt = null,
        facilityUuid = UUID.fromString("2b5ab9f7-6b6c-463d-9a4c-a43b25ccf517"),
        creationFacilityUuid = UUID.fromString("2b5ab9f7-6b6c-463d-9a4c-a43b25ccf517")
    )

    val assignedFacility = TestData.facility(
        uuid = UUID.fromString("637fc349-e3a0-441d-9912-3b632270ce4d"),
        name = "PHC Obvious"
    )

    val patient = TestData.patient(
        uuid = UUID.fromString("da7004ef-d144-42cb-9179-e21e46612842"),
        fullName = "Ramesh Mehta",
        assignedFacilityId = assignedFacility.uuid
    )

    val patientAndAssignedFacility = PatientAndAssignedFacility(patient, assignedFacility)

    val model = defaultModel
        .appointmentLoaded(appointment)
        .patientAndAssignedFacilityLoaded(patientAndAssignedFacility)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showChangeAppointmentButton()
    verify(ui).showAppointmentDate(LocalDate.parse("2018-02-01"))
    verify(ui).showAssignedFacility("PHC Obvious")
    verifyNoMoreInteractions(ui)
  }
}
