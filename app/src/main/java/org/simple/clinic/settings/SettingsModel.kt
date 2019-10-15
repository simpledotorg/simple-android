package org.simple.clinic.settings

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SettingsModel(
    val name: String?,
    val phoneNumber: String?,
    val currentLanguage: Language?
) : Parcelable {

  companion object {
    val FETCHING_USER_DETAILS = SettingsModel(name = null, phoneNumber = null, currentLanguage = null)
  }

  val userDetailsQueried: Boolean
    get() = name != null && phoneNumber != null

  val currentLanguageQueried: Boolean
    get() = currentLanguage != null

  fun userDetailsFetched(name: String, phoneNumber: String): SettingsModel {
    return copy(name = name, phoneNumber = phoneNumber)
  }

  fun currentLanguageFetched(language: Language): SettingsModel {
    return copy(currentLanguage = language)
  }
}
