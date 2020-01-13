package org.simple.clinic.bp.history

import org.simple.clinic.bp.BloodPressureMeasurement

sealed class BloodPressureHistoryScreenEvent

data class BloodPressureHistoryLoaded(val bloodPressures: List<BloodPressureMeasurement>) : BloodPressureHistoryScreenEvent()
