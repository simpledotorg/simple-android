package org.simple.clinic.sync.indicator

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class SyncIndicatorModel : Parcelable {

  companion object {
    fun create(): SyncIndicatorModel {
      return SyncIndicatorModel()
    }
  }
}
