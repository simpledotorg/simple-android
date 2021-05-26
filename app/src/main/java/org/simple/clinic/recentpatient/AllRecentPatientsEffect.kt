package org.simple.clinic.recentpatient

import java.util.UUID

sealed class AllRecentPatientsEffect

object LoadAllRecentPatients : AllRecentPatientsEffect()

data class OpenPatientSummary(val patientUuid: UUID) : AllRecentPatientsEffect()
