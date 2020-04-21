package org.simple.clinic.settings

interface SettingsUi {

  fun displayUserDetails(name: String, phoneNumber: String)

  fun displayCurrentLanguage(language: String)

  fun setChangeLanguageButtonVisible()

  fun displayAppVersion(version: String)

  fun showAppUpdateButton()

  fun hideAppUpdateButton()
}
