package org.simple.clinic.summary.bloodsugar

import java.util.UUID

sealed class BloodSugarSummaryViewEffect

object OpenBloodSugarTypeSelector : BloodSugarSummaryViewEffect()

data class FetchBloodSugarSummary(val patientUuid: UUID) : BloodSugarSummaryViewEffect()
