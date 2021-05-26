package org.simple.clinic.recentpatientsview

import java.util.UUID

sealed class LatestRecentPatientsEffect

data class LoadRecentPatients(val count: Int) : LatestRecentPatientsEffect()

data class OpenPatientSummary(val patientUuid: UUID) : LatestRecentPatientsEffect()

object OpenAllRecentPatientsScreen : LatestRecentPatientsEffect()
