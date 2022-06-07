package org.simple.clinic.home.overdue

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.simple.clinic.home.overdue.PendingListState.SEE_LESS
import org.simple.sharedTestCode.TestData
import java.util.UUID

class OverdueUiRendererTest {

  private val ui = mock<OverdueUi>()
  private val uiRenderer = OverdueUiRenderer(
      ui = ui,
      isOverdueSectionsFeatureEnabled = true
  )
  private val defaultModel = OverdueModel.create()

  @Test
  fun `when overdue appointments are loaded and overdue sections is enabled, then show overdue appointments list, overdue count and hide progress`() {
    // given
    val pendingAppointments = listOf(TestData.overdueAppointment(appointmentUuid = UUID.fromString("b9c7b7f5-a9e4-4589-9cb9-5b92f650d7b0")))
    val agreedToVisitAppointments = listOf(TestData.overdueAppointment(appointmentUuid = UUID.fromString("9cb24c2a-02f9-4eec-aa05-d06ba4fcae82")))
    val removedFromOverdueAppointments = listOf(TestData.overdueAppointment(appointmentUuid = UUID.fromString("f2220d4d-96f7-4f23-9b2f-8f14e59a09df")))
    val overdueAppointmentsLoadedModel = defaultModel
        .currentFacilityLoaded(TestData.facility(uuid = UUID.fromString("b5e72d35-73e2-444b-a266-a02b73a6299a")))
        .overdueAppointmentsLoaded(
            overdueAppointmentSections = OverdueAppointmentSections(
                pendingAppointments = pendingAppointments,
                agreedToVisitAppointments = agreedToVisitAppointments,
                remindToCallLaterAppointments = emptyList(),
                removedFromOverdueAppointments = removedFromOverdueAppointments,
                moreThanAnYearOverdueAppointments = emptyList()
            )
        )

    // when
    uiRenderer.render(overdueAppointmentsLoadedModel)

    // then
    verify(ui).showOverdueAppointments(OverdueAppointmentSections(
        pendingAppointments = pendingAppointments,
        agreedToVisitAppointments = agreedToVisitAppointments,
        remindToCallLaterAppointments = emptyList(),
        removedFromOverdueAppointments = removedFromOverdueAppointments,
        moreThanAnYearOverdueAppointments = emptyList()
    ),
        pendingListState = SEE_LESS)
    verify(ui).showOverdueCount(3)
    verify(ui).hideProgress()
  }

  @Test
  fun `when overdue appointments are not loading or are still loading, then show overdue screen progress state`() {
    // given
    val currentFacilityLoadedModel = defaultModel
        .currentFacilityLoaded(TestData.facility(uuid = UUID.fromString("b5e72d35-73e2-444b-a266-a02b73a6299a")))

    // when
    uiRenderer.render(currentFacilityLoadedModel)

    // then
    verify(ui).showProgress()
  }
}
