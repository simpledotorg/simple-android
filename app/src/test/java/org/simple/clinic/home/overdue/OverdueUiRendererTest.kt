package org.simple.clinic.home.overdue

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
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
    val selectedAppointments = setOf(UUID.fromString("b9c7b7f5-a9e4-4589-9cb9-5b92f650d7b0"), UUID.fromString("f2220d4d-96f7-4f23-9b2f-8f14e59a09df"))
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
        .selectedOverdueAppointmentsChanged(selectedAppointments)
    val overdueListSectionStates = OverdueListSectionStates(
        pendingListState = SEE_LESS,
        isPendingHeaderExpanded = true,
        isAgreedToVisitHeaderExpanded = false,
        isRemindToCallLaterHeaderExpanded = false,
        isRemovedFromOverdueListHeaderExpanded = false,
        isMoreThanAnOneYearOverdueHeader = false
    )

    // when
    uiRenderer.render(overdueAppointmentsLoadedModel)

    // then
    verify(ui).showOverdueAppointments(
        OverdueAppointmentSections(
            pendingAppointments = pendingAppointments,
            agreedToVisitAppointments = agreedToVisitAppointments,
            remindToCallLaterAppointments = emptyList(),
            removedFromOverdueAppointments = removedFromOverdueAppointments,
            moreThanAnYearOverdueAppointments = emptyList()
        ),
        selectedAppointments,
        overdueListSectionStates = overdueListSectionStates
    )
    verify(ui).showOverdueCount(3)
    verify(ui).hideProgress()
    verify(ui).hideNoOverduePatientsView()
    verify(ui).showOverdueRecyclerView()
  }

  @Test
  fun `when overdue appointments are loaded, overdue sections is enabled but overdue section list is empty, then show no overdue appointment view`() {
    // given
    val overdueAppointmentsLoadedModel = defaultModel
        .currentFacilityLoaded(TestData.facility(uuid = UUID.fromString("b5e72d35-73e2-444b-a266-a02b73a6299a")))
        .overdueAppointmentsLoaded(
            overdueAppointmentSections = OverdueAppointmentSections(
                pendingAppointments = emptyList(),
                agreedToVisitAppointments = emptyList(),
                remindToCallLaterAppointments = emptyList(),
                removedFromOverdueAppointments = emptyList(),
                moreThanAnYearOverdueAppointments = emptyList()
            )
        )
    val overdueListSectionStates = OverdueListSectionStates(
        pendingListState = SEE_LESS,
        isPendingHeaderExpanded = true,
        isAgreedToVisitHeaderExpanded = false,
        isRemindToCallLaterHeaderExpanded = false,
        isRemovedFromOverdueListHeaderExpanded = false,
        isMoreThanAnOneYearOverdueHeader = false
    )


    // when
    uiRenderer.render(overdueAppointmentsLoadedModel)

    // then
    verify(ui).showOverdueAppointments(
        OverdueAppointmentSections(
            pendingAppointments = emptyList(),
            agreedToVisitAppointments = emptyList(),
            remindToCallLaterAppointments = emptyList(),
            removedFromOverdueAppointments = emptyList(),
            moreThanAnYearOverdueAppointments = emptyList()
        ),
        emptySet(),
        overdueListSectionStates = overdueListSectionStates
    )
    verify(ui).showOverdueCount(0)
    verify(ui).hideProgress()
    verify(ui).showNoOverduePatientsView()
    verify(ui).hideOverdueRecyclerView()
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
    verify(ui).hideNoOverduePatientsView()
    verify(ui).hideOverdueRecyclerView()
  }

  @Test
  fun `when overdue appointments are selected, then show overdue selected count`() {
    // given
    val selectedAppointments = setOf(
        UUID.fromString("801a16dc-9e6c-464c-aa07-993ae4926489"),
        UUID.fromString("356099c9-b19d-4be4-8e1e-938eeda8ec66")
    )

    val pendingAppointments = listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("801a16dc-9e6c-464c-aa07-993ae4926489")),
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("356099c9-b19d-4be4-8e1e-938eeda8ec66"))
    )
    val selectedAppointmentsModel = defaultModel
        .overdueAppointmentsLoaded(OverdueAppointmentSections(
            pendingAppointments = pendingAppointments,
            agreedToVisitAppointments = emptyList(),
            remindToCallLaterAppointments = emptyList(),
            removedFromOverdueAppointments = emptyList(),
            moreThanAnYearOverdueAppointments = emptyList()
        ))
        .selectedOverdueAppointmentsChanged(selectedAppointments)

    // when
    uiRenderer.render(selectedAppointmentsModel)

    // then
    verify(ui).showOverdueAppointments(
        OverdueAppointmentSections(
            pendingAppointments = pendingAppointments,
            agreedToVisitAppointments = emptyList(),
            remindToCallLaterAppointments = emptyList(),
            removedFromOverdueAppointments = emptyList(),
            moreThanAnYearOverdueAppointments = emptyList()
        ),
        selectedOverdueAppointments = selectedAppointments,
        overdueListSectionStates = selectedAppointmentsModel.overdueListSectionStates
    )
    verify(ui).showOverdueCount(2)
    verify(ui).hideProgress()
    verify(ui).hideNoOverduePatientsView()
    verify(ui).showOverdueRecyclerView()
    verify(ui).showSelectedOverdueAppointmentCount(2)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when no overdue appointments are selected, then hide overdue selected count`() {
    // given
    val pendingAppointments = listOf(
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("801a16dc-9e6c-464c-aa07-993ae4926489")),
        TestData.overdueAppointment(appointmentUuid = UUID.fromString("356099c9-b19d-4be4-8e1e-938eeda8ec66"))
    )
    val selectedAppointmentsModel = defaultModel
        .overdueAppointmentsLoaded(OverdueAppointmentSections(
            pendingAppointments = pendingAppointments,
            agreedToVisitAppointments = emptyList(),
            remindToCallLaterAppointments = emptyList(),
            removedFromOverdueAppointments = emptyList(),
            moreThanAnYearOverdueAppointments = emptyList()
        ))
        .selectedOverdueAppointmentsChanged(emptySet())

    // when
    uiRenderer.render(selectedAppointmentsModel)

    // then
    verify(ui).showOverdueAppointments(
        OverdueAppointmentSections(
            pendingAppointments = pendingAppointments,
            agreedToVisitAppointments = emptyList(),
            remindToCallLaterAppointments = emptyList(),
            removedFromOverdueAppointments = emptyList(),
            moreThanAnYearOverdueAppointments = emptyList()
        ),
        selectedOverdueAppointments = emptySet(),
        overdueListSectionStates = selectedAppointmentsModel.overdueListSectionStates
    )
    verify(ui).showOverdueCount(2)
    verify(ui).hideProgress()
    verify(ui).hideNoOverduePatientsView()
    verify(ui).showOverdueRecyclerView()
    verify(ui).hideSelectedOverdueAppointmentCount()
    verifyNoMoreInteractions(ui)
  }
}
