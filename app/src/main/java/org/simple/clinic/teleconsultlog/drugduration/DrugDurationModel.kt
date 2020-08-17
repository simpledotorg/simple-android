package org.simple.clinic.teleconsultlog.drugduration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DrugDurationModel(
    val duration: String
) : Parcelable {

  companion object {
    fun create(duration: String) = DrugDurationModel(duration)
  }

  val hasDuration: Boolean
    get() = duration.isNotBlank()

  fun durationChanged(duration: String): DrugDurationModel {
    return copy(duration = duration)
  }
}
