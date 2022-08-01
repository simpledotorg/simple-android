package org.simple.clinic.home.overdue.search

import androidx.paging.PagingData
import org.simple.clinic.home.overdue.OverdueAppointment
import java.time.LocalDate
import java.util.UUID

sealed class OverdueSearchEffect

data class ToggleOverdueAppointmentSelection(val appointmentId: UUID) : OverdueSearchEffect()

object LoadSelectedOverdueAppointmentIds : OverdueSearchEffect()

object ClearSelectedOverdueAppointments : OverdueSearchEffect()

data class ReplaceSelectedAppointmentIds(val appointmentIds: Set<UUID>, val type: OverdueButtonType) : OverdueSearchEffect()

object ScheduleDownload : OverdueSearchEffect()

data class SelectAllAppointmentIds(val appointmentIds: Set<UUID>) : OverdueSearchEffect()

data class LoadSearchResultsAppointmentIds(
    val buttonType: OverdueButtonType,
    val searchInputs: List<String>,
    val since: LocalDate
) : OverdueSearchEffect()

object LoadVillageAndPatientNames : OverdueSearchEffect()

data class SearchOverduePatients(
    val searchInputs: List<String>,
    val since: LocalDate
) : OverdueSearchEffect()

sealed class OverdueSearchViewEffect : OverdueSearchEffect()

data class OpenPatientSummary(val patientUuid: UUID) : OverdueSearchViewEffect()

data class OpenContactPatientSheet(val patientUuid: UUID) : OverdueSearchViewEffect()

object OpenSelectDownloadFormatDialog : OverdueSearchViewEffect()

object OpenSelectShareFormatDialog : OverdueSearchViewEffect()

object OpenShareInProgressDialog : OverdueSearchViewEffect()

object ShowNoInternetConnectionDialog : OverdueSearchViewEffect()

data class SetOverdueSearchPagingData(
    val overdueSearchResults: PagingData<OverdueAppointment>,
    val selectedOverdueAppointments: Set<UUID>
) : OverdueSearchViewEffect()
