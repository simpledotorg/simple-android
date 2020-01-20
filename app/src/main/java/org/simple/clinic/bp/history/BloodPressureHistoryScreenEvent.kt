package org.simple.clinic.bp.history

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.widgets.UiEvent

sealed class BloodPressureHistoryScreenEvent : UiEvent

data class BloodPressureHistoryLoaded(val bloodPressures: List<BloodPressureMeasurement>) : BloodPressureHistoryScreenEvent()

object NewBloodPressureClicked : BloodPressureHistoryScreenEvent()

data class BloodPressureClicked(val bloodPressureMeasurement: BloodPressureMeasurement) : BloodPressureHistoryScreenEvent()
