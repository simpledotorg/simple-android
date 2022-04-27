package org.simple.clinic.bp.history

import org.simple.clinic.bp.BloodPressureMeasurement
import java.util.UUID

sealed class BloodPressureHistoryScreenEffect

data class LoadPatient(val patientUuid: UUID) : BloodPressureHistoryScreenEffect()

data class ShowBloodPressures(val patientUuid: UUID) : BloodPressureHistoryScreenEffect()

sealed class BloodPressureHistoryViewEffect : BloodPressureHistoryScreenEffect()

data class OpenBloodPressureEntrySheet(val patientUuid: UUID) : BloodPressureHistoryViewEffect()

data class OpenBloodPressureUpdateSheet(val bloodPressureMeasurement: BloodPressureMeasurement) : BloodPressureHistoryViewEffect()
