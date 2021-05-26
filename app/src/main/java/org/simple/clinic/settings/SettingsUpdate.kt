package org.simple.clinic.settings

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class SettingsUpdate : Update<SettingsModel, SettingsEvent, SettingsEffect> {

  override fun update(
      model: SettingsModel,
      event: SettingsEvent
  ): Next<SettingsModel, SettingsEffect> {
    return when (event) {
      is UserDetailsLoaded -> next(model.userDetailsFetched(name = event.name, phoneNumber = event.phoneNumber))
      is CurrentLanguageLoaded -> next(model.currentLanguageFetched(event.language))
      is ChangeLanguage -> dispatch(OpenLanguageSelectionScreenEffect)
      is AppVersionLoaded -> next(model.appVersionLoaded(appVersion = event.appVersion))
      is AppUpdateAvailabilityChecked -> next(model.checkedAppUpdate(isUpdateAvailable = event.isUpdateAvailable))
    }
  }
}
