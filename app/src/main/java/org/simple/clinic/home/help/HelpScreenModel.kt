package org.simple.clinic.home.help

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class HelpScreenModel : Parcelable {

  companion object {
    fun create() = HelpScreenModel()
  }
}
