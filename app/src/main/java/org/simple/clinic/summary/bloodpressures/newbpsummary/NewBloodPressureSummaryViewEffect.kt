package org.simple.clinic.summary.bloodpressures.newbpsummary

import org.simple.clinic.bp.BloodPressureMeasurement
import java.util.UUID

sealed class NewBloodPressureSummaryViewEffect

data class LoadBloodPressures(val patientUuid: UUID, val numberOfBpsToDisplay: Int) : NewBloodPressureSummaryViewEffect()

data class LoadBloodPressuresCount(val patientUuid: UUID) : NewBloodPressureSummaryViewEffect()

data class OpenBloodPressureEntrySheet(val patientUuid: UUID) : NewBloodPressureSummaryViewEffect()

data class OpenBloodPressureUpdateSheet(val measurement: BloodPressureMeasurement) : NewBloodPressureSummaryViewEffect()

data class ShowBloodPressureHistoryScreen(val patientUuid: UUID) : NewBloodPressureSummaryViewEffect()
