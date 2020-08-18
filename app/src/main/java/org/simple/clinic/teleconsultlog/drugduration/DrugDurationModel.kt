package org.simple.clinic.teleconsultlog.drugduration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.teleconsultlog.drugduration.DrugDurationValidationResult.NOT_VALIDATED

@Parcelize
data class DrugDurationModel(
    val duration: String,
    val validationResult: DrugDurationValidationResult?
) : Parcelable {

  companion object {
    fun create(duration: String) = DrugDurationModel(
        duration = duration,
        validationResult = NOT_VALIDATED
    )
  }

  val hasDuration: Boolean
    get() = duration.isNotBlank()

  val hasValidationResult: Boolean
    get() = validationResult != null

  fun durationChanged(duration: String): DrugDurationModel {
    return copy(duration = duration, validationResult = NOT_VALIDATED)
  }

  fun invalid(result: DrugDurationValidationResult): DrugDurationModel {
    return copy(validationResult = result)
  }
}
