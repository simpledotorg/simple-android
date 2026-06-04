package org.simple.clinic.patientattribute.entry

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patientattribute.BMIReading

@Parcelize
data class BMIEntryModel(
    val height: String,
    val weight: String,
) : Parcelable {
  companion object {
    fun default(bmiReading: BMIReading?) = BMIEntryModel(
        height = bmiReading?.height?.toInt()?.toString().orEmpty(),
        weight = bmiReading?.weight?.toInt()?.toString().orEmpty(),
    )
  }

  fun heightChanged(height: String): BMIEntryModel =
      copy(height = height)

  fun weightChanged(weight: String): BMIEntryModel =
      copy(weight = weight)

  fun deleteWeightLastDigit(): BMIEntryModel = if (weight.isNotEmpty())
    copy(weight = weight.unsafeDropLastChar())
  else
    this

  private fun String.unsafeDropLastChar(): String =
      this.substring(0, this.length - 1)
}
