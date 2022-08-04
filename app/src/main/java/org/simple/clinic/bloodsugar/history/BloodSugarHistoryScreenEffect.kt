package org.simple.clinic.bloodsugar.history

import org.simple.clinic.bloodsugar.BloodSugarHistoryListItemDataSourceFactory
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import java.util.UUID

sealed class BloodSugarHistoryScreenEffect

data class LoadPatient(val patientUuid: UUID) : BloodSugarHistoryScreenEffect()

data class LoadBloodSugarHistory(val patientUuid: UUID) : BloodSugarHistoryScreenEffect()

sealed class BloodSugarHistoryScreenViewEffect : BloodSugarHistoryScreenEffect()

data class OpenBloodSugarEntrySheet(val patientUuid: UUID) : BloodSugarHistoryScreenViewEffect()

data class OpenBloodSugarUpdateSheet(val bloodSugarMeasurement: BloodSugarMeasurement) : BloodSugarHistoryScreenViewEffect()

data class ShowBloodSugars(val bloodSugarHistoryListItemDataSourceFactory: BloodSugarHistoryListItemDataSourceFactory) : BloodSugarHistoryScreenViewEffect()
