package org.simple.clinic.bp.history

import org.simple.clinic.bp.BloodPressureMeasurement
import java.util.UUID

sealed class BloodPressureHistoryScreenEffect

data class LoadPatient(val patientUuid: UUID) : BloodPressureHistoryScreenEffect()

data class LoadBloodPressureHistory(val patientUuid: UUID) : BloodPressureHistoryScreenEffect()

object OpenBloodPressureEntrySheet : BloodPressureHistoryScreenEffect()

data class OpenBloodPressureUpdateSheet(val bloodPressureMeasurement: BloodPressureMeasurement) : BloodPressureHistoryScreenEffect()
