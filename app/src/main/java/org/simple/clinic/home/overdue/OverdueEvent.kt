package org.simple.clinic.home.overdue

import androidx.paging.PagingData
import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class OverdueEvent : UiEvent

data class CurrentFacilityLoaded(val facility: Facility) : OverdueEvent()

data class CallPatientClicked(val patientUuid: UUID) : OverdueEvent() {
  override val analyticsName = "Overdue Screen:Call Patient clicked"
}

data class OverduePatientClicked(val patientUuid: UUID) : OverdueEvent() {
  override val analyticsName = "Overdue Screen:Patient name clicked"
}

data class OverdueAppointmentsLoaded(
    val overdueAppointments: PagingData<OverdueAppointment>
) : OverdueEvent()
