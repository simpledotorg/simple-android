package org.simple.clinic.bp

import org.simple.clinic.R
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional

enum class BloodPressureLevel(private val urgency: Int, val displayTextRes: Optional<Int>) {

  EXTREMELY_HIGH(4, Just(R.string.bloodpressure_level_high)),

  VERY_HIGH(3, Just(R.string.bloodpressure_level_high)),

  MODERATELY_HIGH(2, Just(R.string.bloodpressure_level_high)),

  MILDLY_HIGH(1, None()),

  NORMAL(0, None()),

  LOW(-1, Just(R.string.bloodpressure_level_low));

  val isHigh: Boolean
    get() = when (this) {
      LOW, NORMAL, MILDLY_HIGH -> false
      MODERATELY_HIGH, VERY_HIGH, EXTREMELY_HIGH -> true
    }

  companion object {

    fun compute(measurement: BloodPressureMeasurement): BloodPressureLevel {
      val systolicLevel = computeSystolic(measurement)
      val diastolicLevel = computeDiastolic(measurement)

      return if (systolicLevel.urgency > diastolicLevel.urgency) {
        systolicLevel
      } else {
        diastolicLevel
      }
    }

    private fun computeSystolic(measurement: BloodPressureMeasurement): BloodPressureLevel {
      return measurement.reading.systolic.let {
        when {
          it <= 89 -> LOW
          it in 90..129 -> NORMAL
          it in 130..139 -> MILDLY_HIGH
          it in 140..159 -> MODERATELY_HIGH
          it in 160..199 -> VERY_HIGH
          it >= 200 -> EXTREMELY_HIGH
          else -> throw AssertionError("Shouldn't reach here: $measurement")
        }
      }
    }

    private fun computeDiastolic(measurement: BloodPressureMeasurement): BloodPressureLevel {
      return measurement.reading.diastolic.let {
        when {
          it <= 59 -> LOW
          it in 60..79 -> NORMAL
          it in 80..89 -> MILDLY_HIGH
          it in 90..99 -> MODERATELY_HIGH
          it in 100..119 -> VERY_HIGH
          it >= 120 -> EXTREMELY_HIGH
          else -> throw AssertionError("Shouldn't reach here: $measurement")
        }
      }
    }
  }
}
