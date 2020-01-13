package org.simple.clinic.bp.history

import java.util.UUID
import org.simple.clinic.bp.BloodPressureMeasurement

sealed class BloodPressureHistoryScreenEffect

data class LoadBloodPressureHistory(val patientUuid: UUID) : BloodPressureHistoryScreenEffect()

object OpenBloodPressureEntrySheet : BloodPressureHistoryScreenEffect()

data class OpenBloodPressureUpdateSheet(val bloodPressureMeasurement: BloodPressureMeasurement) : BloodPressureHistoryScreenEffect()
