package org.simple.clinic.drugs

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class OpenIntention : Parcelable {

  @Parcelize
  object RefillMedicine : OpenIntention() {
    override fun toString(): String = "REFILL_MEDICINE"
  }

  @Parcelize
  object AddNewMedicine : OpenIntention() {
    override fun toString(): String = "NEW_MEDICINE"
  }
}
