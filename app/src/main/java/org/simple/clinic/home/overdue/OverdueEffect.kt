package org.simple.clinic.home.overdue

import androidx.paging.PagingData
import org.simple.clinic.facility.Facility
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

data class OpenContactPatientScreen(val patientUuid: UUID) : OverdueEffect()

data class OpenPatientSummary(val patientUuid: UUID) : OverdueEffect()

data class ShowOverdueAppointments(
    val overdueAppointments: PagingData<OverdueAppointment>,
    val isDiabetesManagementEnabled: Boolean
) : OverdueEffect()

object ShowNoActiveNetworkConnectionDialog : OverdueEffect()
