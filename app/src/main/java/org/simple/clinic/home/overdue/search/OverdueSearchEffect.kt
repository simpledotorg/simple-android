package org.simple.clinic.home.overdue.search

import java.time.LocalDate
import java.util.UUID

sealed class OverdueSearchEffect

data class ToggleOverdueAppointmentSelection(val appointmentId: UUID) : OverdueSearchEffect()

data object LoadSelectedOverdueAppointmentIds : OverdueSearchEffect()

data object ClearSelectedOverdueAppointments : OverdueSearchEffect()

data class ReplaceSelectedAppointmentIds(val appointmentIds: Set<UUID>, val type: OverdueButtonType) : OverdueSearchEffect()

data object ScheduleDownload : OverdueSearchEffect()

data class SelectAllAppointmentIds(val appointmentIds: Set<UUID>) : OverdueSearchEffect()

data class LoadSearchResultsAppointmentIds(
    val buttonType: OverdueButtonType,
    val searchInputs: List<String>,
    val since: LocalDate
) : OverdueSearchEffect()

data object LoadVillageAndPatientNames : OverdueSearchEffect()

data class SearchOverduePatients(
    val searchInputs: List<String>,
    val since: LocalDate
) : OverdueSearchEffect()

sealed class OverdueSearchViewEffect : OverdueSearchEffect()

data class OpenPatientSummary(val patientUuid: UUID) : OverdueSearchViewEffect()

data class OpenContactPatientSheet(val patientUuid: UUID) : OverdueSearchViewEffect()

data object OpenSelectDownloadFormatDialog : OverdueSearchViewEffect()

data object OpenSelectShareFormatDialog : OverdueSearchViewEffect()

data object OpenShareInProgressDialog : OverdueSearchViewEffect()

data object ShowNoInternetConnectionDialog : OverdueSearchViewEffect()
