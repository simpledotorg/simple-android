package org.simple.clinic.home.overdue.search

import java.time.LocalDate
import java.util.UUID

sealed class OverdueSearchEffect

object LoadOverdueSearchHistory : OverdueSearchEffect()

data class ValidateOverdueSearchQuery(val searchQuery: String) : OverdueSearchEffect()

data class AddQueryToOverdueSearchHistory(val searchQuery: String) : OverdueSearchEffect()

data class SearchOverduePatients(val searchQuery: String, val since: LocalDate) : OverdueSearchEffect()

data class ToggleOverdueAppointmentSelection(val appointmentId: UUID) : OverdueSearchEffect()

object LoadSelectedOverdueAppointmentIds : OverdueSearchEffect()

object ClearSelectedOverdueAppointments : OverdueSearchEffect()

data class ReplaceSelectedAppointmentIds(val appointmentIds: Set<UUID>, val type: OverdueButtonType) : OverdueSearchEffect()

object ScheduleDownload : OverdueSearchEffect()

sealed class OverdueSearchViewEffect : OverdueSearchEffect()

data class OpenPatientSummary(val patientUuid: UUID) : OverdueSearchViewEffect()

data class OpenContactPatientSheet(val patientUuid: UUID) : OverdueSearchViewEffect()

data class SetOverdueSearchQuery(val searchQuery: String) : OverdueSearchViewEffect()

object OpenSelectDownloadFormatDialog : OverdueSearchViewEffect()

object OpenSelectShareFormatDialog : OverdueSearchViewEffect()

object OpenShareInProgressDialog : OverdueSearchViewEffect()

object ShowNoInternetConnectionDialog : OverdueSearchViewEffect()

data class SelectAllAppointmentIds(val appointmentIds: Set<UUID>) : OverdueSearchEffect()
