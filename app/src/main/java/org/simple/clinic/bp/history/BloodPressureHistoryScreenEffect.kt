package org.simple.clinic.bp.history

import java.util.UUID

sealed class BloodPressureHistoryScreenEffect

data class LoadBloodPressureHistory(val patientUuid: UUID) : BloodPressureHistoryScreenEffect()

object OpenBloodPressureEntrySheet : BloodPressureHistoryScreenEffect()
