package org.simple.clinic.bloodsugar

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BloodSugarReading(val value: String, val type: BloodSugarMeasurementType) : Parcelable {
  val isHigh: Boolean
    get() {
      return when (type) {
        Random,
        PostPrandial -> value.toFloat() >= 200
        Fasting -> value.toFloat() >= 126
        HbA1c -> value.toFloat() >= 7.0
        else -> false
      }
    }

  val displayValue: String
    get() {
      return when (type) {
        Random -> value.toFloat().toInt().toString()
        PostPrandial -> value.toFloat().toInt().toString()
        Fasting -> value.toFloat().toInt().toString()
        HbA1c -> value.toFloat().toString()
        is Unknown -> value.toInt().toString()
      }
    }

  val displayUnitSeparator: String
    get() {
      return when (type) {
        Random, PostPrandial, Fasting, is Unknown -> " "
        HbA1c -> ""
      }
    }

  fun readingChanged(newReading: String): BloodSugarReading = copy(value = newReading)
}
