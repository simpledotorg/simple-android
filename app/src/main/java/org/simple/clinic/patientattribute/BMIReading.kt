package org.simple.clinic.patientattribute

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.lang.Math.round

@Parcelize
data class BMIReading(val height: Float, val weight: Float) : Parcelable {
  fun calculateBMI(): Float? {
    if (height <= 0 || weight <= 0) return null

    val heightInMeters = height / 100f
    val bmi = weight / (heightInMeters * heightInMeters)
    val factor = 100f
    val roundedBmi = round(bmi * factor) / factor
    return roundedBmi
  }
}
