package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.appupdate.AppUpdateHelpContact
import java.util.Optional

data class CriticalAppUpdateModel(
    val appUpdateHelpContact: Optional<AppUpdateHelpContact>,
    val appStaleness: Int?
) {

  companion object {

    fun create() = CriticalAppUpdateModel(
        appUpdateHelpContact = Optional.empty(),
        appStaleness = null
    )
  }

  val contactUrl: String
    get() = appUpdateHelpContact.get().url

  val hasHelpContact: Boolean
    get() = appUpdateHelpContact.isPresent

  val hasAppStaleness: Boolean
    get() = appStaleness != null

  fun appUpdateHelpContactLoaded(appUpdateHelpContact: Optional<AppUpdateHelpContact>): CriticalAppUpdateModel {
    return copy(appUpdateHelpContact = appUpdateHelpContact)
  }

  fun appStalenessLoaded(appStaleness: Int): CriticalAppUpdateModel {
    return copy(appStaleness = appStaleness)
  }
}
