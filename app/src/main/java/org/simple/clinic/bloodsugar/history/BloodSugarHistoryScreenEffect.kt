package org.simple.clinic.bloodsugar.history

import java.util.UUID

sealed class BloodSugarHistoryScreenEffect

data class LoadPatient(val patientUuid: UUID) : BloodSugarHistoryScreenEffect()

data class LoadBloodSugarHistory(val patientUuid: UUID) : BloodSugarHistoryScreenEffect()
