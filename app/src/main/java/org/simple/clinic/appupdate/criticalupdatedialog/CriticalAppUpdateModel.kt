package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.appupdate.AppUpdateHelpContact
import java.util.Optional

data class CriticalAppUpdateModel(
    val appUpdateHelpContact: Optional<AppUpdateHelpContact>
) {

  companion object {

    fun create() = CriticalAppUpdateModel(
        appUpdateHelpContact = Optional.empty()
    )
  }

  fun appUpdateHelpContactLoaded(appUpdateHelpContact: Optional<AppUpdateHelpContact>): CriticalAppUpdateModel {
    return copy(appUpdateHelpContact = appUpdateHelpContact)
  }
}
