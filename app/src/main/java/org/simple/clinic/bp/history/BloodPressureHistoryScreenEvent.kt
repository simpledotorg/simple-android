package org.simple.clinic.bp.history

import org.simple.clinic.bp.BloodPressureMeasurement

sealed class BloodPressureHistoryScreenEvent

data class BloodPressureHistoryLoaded(val bloodPressures: List<BloodPressureMeasurement>) : BloodPressureHistoryScreenEvent()

object NewBloodPressureClicked : BloodPressureHistoryScreenEvent()

data class BloodPressureClicked(val bloodPressureMeasurement: BloodPressureMeasurement) : BloodPressureHistoryScreenEvent()
