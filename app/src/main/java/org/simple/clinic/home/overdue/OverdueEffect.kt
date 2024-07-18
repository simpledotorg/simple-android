package org.simple.clinic.home.overdue

import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.download.OverdueListFileFormat
import java.time.LocalDate
import java.util.UUID

sealed class OverdueEffect

data object LoadCurrentFacility : OverdueEffect()

data class LoadOverdueAppointments(
    val overdueSince: LocalDate,
    val facility: Facility
) : OverdueEffect()

data class ScheduleDownload(val fileFormat: OverdueListFileFormat) : OverdueEffect()

data class ToggleOverdueAppointmentSelection(val appointmentId: UUID) : OverdueEffect()

data object LoadSelectedOverdueAppointmentIds : OverdueEffect()

data object ClearSelectedOverdueAppointments : OverdueEffect()

sealed class OverdueViewEffect : OverdueEffect()

data class OpenContactPatientScreen(val patientUuid: UUID) : OverdueViewEffect()

data class OpenPatientSummary(val patientUuid: UUID) : OverdueViewEffect()

data object ShowNoActiveNetworkConnectionDialog : OverdueViewEffect()

data object OpenSelectDownloadFormatDialog : OverdueViewEffect()

data object OpenSelectShareFormatDialog : OverdueViewEffect()

data object OpenSharingInProgressDialog : OverdueViewEffect()

data object OpenOverdueSearch : OverdueViewEffect()
