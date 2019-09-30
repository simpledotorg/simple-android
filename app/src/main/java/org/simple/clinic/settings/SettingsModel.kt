package org.simple.clinic.settings

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SettingsModel(
    val userDetailsQueried: Boolean,
    val name: String?,
    val phoneNumber: String?
): Parcelable {

  companion object {
    val FETCHING_USER_DETAILS = SettingsModel(userDetailsQueried = false, name = null, phoneNumber = null)
  }

  fun userDetailsFetched(name: String, phoneNumber: String): SettingsModel {
    return this.copy(name = name, phoneNumber = phoneNumber, userDetailsQueried = true)
  }
}
