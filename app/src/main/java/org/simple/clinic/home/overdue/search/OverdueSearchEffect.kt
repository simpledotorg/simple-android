package org.simple.clinic.home.overdue.search

import org.simple.clinic.overdue.download.OverdueListFileFormat
import java.time.LocalDate
import java.util.UUID

sealed class OverdueSearchEffect

object LoadOverdueSearchHistory : OverdueSearchEffect()

data class ValidateOverdueSearchQuery(val searchQuery: String) : OverdueSearchEffect()

data class AddQueryToOverdueSearchHistory(val searchQuery: String) : OverdueSearchEffect()

data class SearchOverduePatients(val searchQuery: String, val since: LocalDate) : OverdueSearchEffect()

data class ScheduleDownload(val fileFormat: OverdueListFileFormat, val selectedAppointmentIds: Set<UUID>) : OverdueSearchEffect()

sealed class OverdueSearchViewEffect : OverdueSearchEffect()

data class OpenPatientSummary(val patientUuid: UUID) : OverdueSearchViewEffect()

data class OpenContactPatientSheet(val patientUuid: UUID) : OverdueSearchViewEffect()

data class SetOverdueSearchQuery(val searchQuery: String) : OverdueSearchViewEffect()

data class OpenSelectDownloadFormatDialog(val selectedAppointmentIds: Set<UUID>) : OverdueSearchViewEffect()

object ShowNoActiveNetworkConnectionDialog : OverdueSearchViewEffect()
