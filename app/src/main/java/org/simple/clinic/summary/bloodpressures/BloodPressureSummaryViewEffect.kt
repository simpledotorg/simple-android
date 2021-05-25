package org.simple.clinic.summary.bloodpressures

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.Facility
import java.util.UUID

sealed class BloodPressureSummaryViewEffect

data class LoadBloodPressures(
    val patientUuid: UUID,
    val numberOfBpsToDisplay: Int
) : BloodPressureSummaryViewEffect()

data class LoadBloodPressuresCount(val patientUuid: UUID) : BloodPressureSummaryViewEffect()

data class OpenBloodPressureEntrySheet(
    val patientUuid: UUID,
    val currentFacility: Facility
) : BloodPressureSummaryViewEffect()

data class OpenBloodPressureUpdateSheet(val measurement: BloodPressureMeasurement) : BloodPressureSummaryViewEffect()

data class ShowBloodPressureHistoryScreen(val patientUuid: UUID) : BloodPressureSummaryViewEffect()

object LoadCurrentFacility : BloodPressureSummaryViewEffect()
