package org.simple.clinic.bp.history.adapter

import org.simple.clinic.bp.BloodPressureMeasurement

sealed class Event {
  data object AddNewBpClicked : Event()
  data class BloodPressureHistoryItemClicked(val measurement: BloodPressureMeasurement) : Event()
}
