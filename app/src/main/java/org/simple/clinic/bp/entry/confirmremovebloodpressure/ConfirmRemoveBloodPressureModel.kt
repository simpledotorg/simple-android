package org.simple.clinic.bp.entry.confirmremovebloodpressure

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ConfirmRemoveBloodPressureModel : Parcelable {

  companion object {
    fun create() = ConfirmRemoveBloodPressureModel()
  }
}
