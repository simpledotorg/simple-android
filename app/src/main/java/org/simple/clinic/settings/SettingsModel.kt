package org.simple.clinic.settings

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SettingsModel(
    val name: String?,
    val phoneNumber: String?
) : Parcelable {

  companion object {
    val FETCHING_USER_DETAILS = SettingsModel(name = null, phoneNumber = null)
  }

  val userDetailsQueried: Boolean
    get() = name != null && phoneNumber != null

  fun userDetailsFetched(name: String, phoneNumber: String): SettingsModel {
    return this.copy(name = name, phoneNumber = phoneNumber)
  }
}
