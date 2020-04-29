package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import java.util.UUID

sealed class BloodSugarSummaryViewEffect

object OpenBloodSugarTypeSelector : BloodSugarSummaryViewEffect()

data class FetchBloodSugarSummary(val patientUuid: UUID) : BloodSugarSummaryViewEffect()

data class FetchBloodSugarCount(val patientUuid: UUID) : BloodSugarSummaryViewEffect()

data class ShowBloodSugarHistoryScreen(val patientUuid: UUID) : BloodSugarSummaryViewEffect()

data class OpenBloodSugarUpdateSheet(val measurement: BloodSugarMeasurement) : BloodSugarSummaryViewEffect()
