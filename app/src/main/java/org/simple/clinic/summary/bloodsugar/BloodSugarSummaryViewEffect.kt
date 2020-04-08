package org.simple.clinic.summary.bloodsugar

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.facility.Facility
import java.util.UUID

sealed class BloodSugarSummaryViewEffect

data class OpenBloodSugarTypeSelector(val currentFacility: Facility) : BloodSugarSummaryViewEffect()

data class FetchBloodSugarSummary(val patientUuid: UUID) : BloodSugarSummaryViewEffect()

data class FetchBloodSugarCount(val patientUuid: UUID) : BloodSugarSummaryViewEffect()

object FetchCurrentFacility : BloodSugarSummaryViewEffect()

data class ShowBloodSugarHistoryScreen(val patientUuid: UUID) : BloodSugarSummaryViewEffect()

data class OpenBloodSugarUpdateSheet(val measurement: BloodSugarMeasurement) : BloodSugarSummaryViewEffect()
