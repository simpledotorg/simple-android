package org.simple.clinic.patientattribute

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

@Parcelize
data class BMIReading(val height: Float, val weight: Float) : Parcelable {
  fun calculateBMI(): Float? {
    if (height <= 0 || weight <= 0) return null

    val heightInMeters = height / 100
    val bmi = weight / (heightInMeters * heightInMeters)
    return bmi
  }
}
