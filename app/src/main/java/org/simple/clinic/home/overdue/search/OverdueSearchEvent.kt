package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result
import java.util.UUID

sealed class OverdueSearchEvent

data class OverdueSearchHistoryLoaded(val searchHistory: Set<String>) : OverdueSearchEvent()

data class OverdueSearchQueryChanged(val searchQuery: String) : OverdueSearchEvent()

data class OverdueSearchQueryValidated(val result: Result) : OverdueSearchEvent()

data class OverdueSearchResultsLoaded(val overdueAppointments: PagingData<OverdueAppointment>) : OverdueSearchEvent()

data class OverduePatientClicked(val patientUuid: UUID) : OverdueSearchEvent()

data class CallPatientClicked(val patientUuid: UUID) : OverdueSearchEvent()
