package org.simple.clinic.patientattribute

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class BMIReading(val height: String, val weight: String) : Parcelable {

  fun calculateBMI(): Int? {
    if (height.isBlank() || weight.isBlank()) return null

    try {
      val heightInMeters = height.toDouble() / 100
      val weightInKg = weight.toDouble()

      if (heightInMeters <= 0 || weightInKg <= 0) return null

      return (weightInKg / (heightInMeters * heightInMeters)).roundToInt()
    } catch (e: NumberFormatException) {
      return null
    }
  }
}
