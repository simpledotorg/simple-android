package org.simple.clinic.teleconsultlog.drugduration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DrugDurationModel(
    val duration: String,
    val validationResult: DrugDurationValidationResult?
) : Parcelable {

  companion object {
    fun create(duration: String) = DrugDurationModel(
        duration = duration,
        validationResult = null
    )
  }

  val hasDuration: Boolean
    get() = duration.isNotBlank()

  val hasValidationResult: Boolean
    get() = validationResult != null

  fun durationChanged(duration: String): DrugDurationModel {
    return copy(duration = duration, validationResult = null)
  }

  fun durationInvalid(result: DrugDurationValidationResult): DrugDurationModel {
    return copy(validationResult = result)
  }
}
