package org.simple.clinic.home

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class HomeScreenModel : Parcelable {

  companion object {
    fun create() = HomeScreenModel()
  }
}
