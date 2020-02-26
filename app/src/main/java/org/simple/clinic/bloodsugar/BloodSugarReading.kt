package org.simple.clinic.bloodsugar

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BloodSugarReading(val value: String, val type: BloodSugarMeasurementType) : Parcelable {
  val isHigh: Boolean
    get() {
      return when (type) {
        Random,
        PostPrandial -> value.toInt() >= 200
        Fasting -> value.toInt() >= 126
        HbA1c -> value.toFloat() >= 7.0
        else -> false
      }
    }

  val displayValue: String
    get() {
      return when (type) {
        Random -> value.toInt().toString()
        PostPrandial -> value.toInt().toString()
        Fasting -> value.toInt().toString()
        HbA1c -> value.toFloat().toString()
        is Unknown -> value.toInt().toString()
      }
    }
}
