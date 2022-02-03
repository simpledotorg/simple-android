package org.simple.clinic.recentpatient

import androidx.paging.PagingData
import org.simple.clinic.patient.RecentPatient
import java.util.UUID

sealed class AllRecentPatientsEffect

object LoadAllRecentPatients : AllRecentPatientsEffect()

sealed class AllRecentPatientsViewEffect : AllRecentPatientsEffect()

data class OpenPatientSummary(val patientUuid: UUID) : AllRecentPatientsViewEffect()

data class ShowRecentPatients(val recentPatients: PagingData<RecentPatient>) : AllRecentPatientsViewEffect()
