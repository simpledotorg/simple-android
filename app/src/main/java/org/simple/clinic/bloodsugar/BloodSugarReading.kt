package org.simple.clinic.bloodsugar

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BloodSugarReading(val value: Float, val type: BloodSugarMeasurementType) : Parcelable {
  val isHigh: Boolean
    get() {
      return when (type) {
        Random,
        PostPrandial -> value >= 200
        Fasting -> value >= 126
        HbA1c -> value >= 7
        else -> false
      }
    }

  val displayValue: String
    get() {
      return when (type) {
        Random -> value.toInt().toString()
        PostPrandial -> value.toInt().toString()
        Fasting -> value.toInt().toString()
        HbA1c -> value.toString()
        is Unknown -> value.toInt().toString()
      }
    }
}
