package org.simple.clinic.security.pin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PinEntryModel: Parcelable {

  companion object {
    fun default(): PinEntryModel {
      return PinEntryModel()
    }
  }
}
