package org.simple.clinic.main

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TheActivityModel(val isFreshLogin: Boolean) : Parcelable {

  companion object {
    fun createForAlreadyLoggedInUser(): TheActivityModel = TheActivityModel(isFreshLogin = false)

    fun createForNewlyLoggedInUser(): TheActivityModel = TheActivityModel(isFreshLogin = true)
  }
}
