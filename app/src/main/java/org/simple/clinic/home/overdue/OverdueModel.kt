package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class OverdueModel: Parcelable {

  companion object {
    fun create(): OverdueModel = OverdueModel()
  }
}
