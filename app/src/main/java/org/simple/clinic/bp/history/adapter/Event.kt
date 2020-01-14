package org.simple.clinic.bp.history.adapter

import org.simple.clinic.bp.BloodPressureMeasurement

sealed class Event {
  data class BloodPressureClicked(val measurement: BloodPressureMeasurement) : Event()
}
