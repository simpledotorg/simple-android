package org.simple.clinic.bp

import org.simple.clinic.R
import org.simple.clinic.appconfig.Country
import java.util.Optional

enum class BloodPressureLevel(private val urgency: Int, val displayTextRes: Optional<Int>) {

  EXTREMELY_HIGH(4, Optional.of(R.string.bloodpressure_level_high)),

  VERY_HIGH(3, Optional.of(R.string.bloodpressure_level_high)),

  MODERATELY_HIGH(2, Optional.of(R.string.bloodpressure_level_high)),

  MILDLY_HIGH(1, Optional.empty()),

  NORMAL(0, Optional.empty()),

  LOW(-1, Optional.of(R.string.bloodpressure_level_low));

  companion object {

    fun isHigh(
        measurement: BloodPressureMeasurement,
        country: Country,
        hasDiabetes: Boolean
    ): Boolean {
      return when (compute(measurement)) {
        LOW, NORMAL -> false
        MILDLY_HIGH -> country.isoCountryCode == Country.BANGLADESH && hasDiabetes
        MODERATELY_HIGH, VERY_HIGH, EXTREMELY_HIGH -> true
      }
    }

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
