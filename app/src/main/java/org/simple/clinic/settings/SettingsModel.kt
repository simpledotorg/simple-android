package org.simple.clinic.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingsModel(
    val name: String?,
    val phoneNumber: String?,
    val currentLanguage: Language?,
    val appVersion: String?,
    val isUpdateAvailable: Boolean?,
    val isUserLoggingOut: Boolean?
) : Parcelable {

  companion object {
    fun default() = SettingsModel(
        name = null,
        phoneNumber = null,
        currentLanguage = null,
        appVersion = null,
        isUpdateAvailable = null,
        isUserLoggingOut = null
    )
  }

  val userDetailsQueried: Boolean
    get() = name != null && phoneNumber != null

  val currentLanguageQueried: Boolean
    get() = currentLanguage != null

  val appVersionQueried: Boolean
    get() = appVersion != null

  fun userDetailsFetched(name: String, phoneNumber: String): SettingsModel {
    return copy(name = name, phoneNumber = phoneNumber)
  }

  fun currentLanguageFetched(language: Language): SettingsModel {
    return copy(currentLanguage = language)
  }

  fun appVersionLoaded(appVersion: String): SettingsModel {
    return copy(appVersion = appVersion)
  }

  fun checkedAppUpdate(isUpdateAvailable: Boolean): SettingsModel {
    return copy(isUpdateAvailable = isUpdateAvailable)
  }

  fun userLoggingOut(): SettingsModel {
    return copy(isUserLoggingOut = true)
  }

  fun userLoggedOut(): SettingsModel {
    return copy(isUserLoggingOut = false)
  }

  fun userLogoutFailed(): SettingsModel {
    return copy(isUserLoggingOut = false)
  }
}
