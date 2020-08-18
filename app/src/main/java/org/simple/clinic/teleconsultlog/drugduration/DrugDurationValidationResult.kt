package org.simple.clinic.teleconsultlog.drugduration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class DrugDurationValidationResult : Parcelable {
  NOT_VALIDATED,
  BLANK
}
