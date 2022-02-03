package org.simple.clinic.recentpatient

import androidx.paging.PagingData
import org.simple.clinic.patient.RecentPatient
import java.util.UUID

sealed class AllRecentPatientsEffect

object LoadAllRecentPatients : AllRecentPatientsEffect()

data class OpenPatientSummary(val patientUuid: UUID) : AllRecentPatientsEffect()

data class ShowRecentPatients(val recentPatients: PagingData<RecentPatient>) : AllRecentPatientsEffect()

sealed class AllRecentPatientsViewEffect : AllRecentPatientsEffect()
