package org.simple.clinic.bloodsugar.history

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import java.util.UUID

sealed class BloodSugarHistoryScreenEffect

data class LoadPatient(val patientUuid: UUID) : BloodSugarHistoryScreenEffect()

data class OpenBloodSugarEntrySheet(val patientUuid: UUID) : BloodSugarHistoryScreenEffect()

data class OpenBloodSugarUpdateSheet(val bloodSugarMeasurement: BloodSugarMeasurement) : BloodSugarHistoryScreenEffect()

data class ShowBloodSugars(val patientUuid: UUID) : BloodSugarHistoryScreenEffect()
