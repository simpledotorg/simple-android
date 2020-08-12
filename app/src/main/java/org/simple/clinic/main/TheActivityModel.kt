package org.simple.clinic.main

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class TheActivityModel : Parcelable {

  companion object {
    fun create(): TheActivityModel = TheActivityModel()
  }
}
