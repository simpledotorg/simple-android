package org.simple.clinic.home.overdue

import androidx.paging.PagingData
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.download.OverdueListFileFormat
import java.time.LocalDate
import java.util.UUID

sealed class OverdueEffect

object LoadCurrentFacility : OverdueEffect()

data class LoadOverdueAppointments_old(
    val overdueSince: LocalDate,
    val facility: Facility
) : OverdueEffect()

data class LoadOverdueAppointments(
    val overdueSince: LocalDate,
    val facility: Facility
) : OverdueEffect()

data class ScheduleDownload(val fileFormat: OverdueListFileFormat) : OverdueEffect()

sealed class OverdueViewEffect : OverdueEffect()

data class OpenContactPatientScreen(val patientUuid: UUID) : OverdueViewEffect()

data class OpenPatientSummary(val patientUuid: UUID) : OverdueViewEffect()

data class ShowOverdueAppointments(
    val overdueAppointments: PagingData<OverdueAppointment>,
    val isDiabetesManagementEnabled: Boolean
) : OverdueViewEffect()

object ShowNoActiveNetworkConnectionDialog : OverdueViewEffect()

object OpenSelectDownloadFormatDialog : OverdueViewEffect()

object OpenSelectShareFormatDialog : OverdueViewEffect()

object OpenSharingInProgressDialog : OverdueViewEffect()
