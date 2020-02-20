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
        else -> false
      }
    }
}
