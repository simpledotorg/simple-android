package org.simple.clinic.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingsModel(
    val applicationId: String,
    val name: String?,
    val phoneNumber: String?,
    val currentLanguage: Language?,
    val appVersion: String?,
    val isUpdateAvailable: Boolean?
) : Parcelable {

  companion object {
    fun default(applicationId: String) = SettingsModel(
        applicationId = applicationId,
        name = null,
        phoneNumber = null,
        currentLanguage = null,
        appVersion = null,
        isUpdateAvailable = null
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
}
