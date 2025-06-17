package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import java.util.UUID

sealed class BloodSugarSummaryViewEffect

data object OpenBloodSugarTypeSelector : BloodSugarSummaryViewEffect()

data class FetchBloodSugarSummary(val patientUuid: UUID) : BloodSugarSummaryViewEffect()

data class FetchBloodSugarCount(val patientUuid: UUID) : BloodSugarSummaryViewEffect()

data class ShowBloodSugarHistoryScreen(val patientUuid: UUID) : BloodSugarSummaryViewEffect()

data class OpenBloodSugarUpdateSheet(val id: UUID, val measurementType: BloodSugarMeasurementType) : BloodSugarSummaryViewEffect()
