package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class OverdueSearchEvent : UiEvent

data class OverdueSearchHistoryLoaded(val searchHistory: Set<String>) : OverdueSearchEvent()

data class OverdueSearchQueryChanged(val searchQuery: String) : OverdueSearchEvent()

data class OverdueSearchQueryValidated(val result: Result) : OverdueSearchEvent()

data class OverdueSearchResultsLoaded(val overdueAppointments: PagingData<OverdueAppointment>) : OverdueSearchEvent()

data class OverduePatientClicked(val patientUuid: UUID) : OverdueSearchEvent()

data class CallPatientClicked(val patientUuid: UUID) : OverdueSearchEvent()

data class OverdueSearchHistoryClicked(val searchQuery: String) : OverdueSearchEvent()

data class OverdueSearchLoadStateChanged(val overdueSearchProgressState: OverdueSearchProgressState) : OverdueSearchEvent()

object OverdueSearchScreenShown : OverdueSearchEvent()

data class OverdueAppointmentCheckBoxClicked(val appointmentId: UUID) : OverdueSearchEvent()

object ClearSelectedOverdueAppointmentsClicked : OverdueSearchEvent()

data class SelectedOverdueAppointmentsLoaded(val selectedAppointmentIds: Set<UUID>) : OverdueSearchEvent()

data class SelectedAppointmentIdsReplaced(val type: OverdueButtonType) : OverdueSearchEvent()

data class DownloadButtonClicked(val searchResultsAppointmentIds: Set<UUID>) : OverdueSearchEvent() {

  override val analyticsName = "Overdue Search Screen:Download Clicked"
}

data class ShareButtonClicked(val searchResultsAppointmentIds: Set<UUID>) : OverdueSearchEvent() {

  override val analyticsName = "Overdue Search Screen:Share Clicked"
}
