package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.appupdate.AppUpdateHelpContact
import org.simple.clinic.appupdate.AppUpdateNudgePriority
import java.util.Optional

data class CriticalAppUpdateModel(
    val appUpdateHelpContact: Optional<AppUpdateHelpContact>,
    val appStaleness: Int?,
    val appUpdateNudgePriority: AppUpdateNudgePriority
) {

  companion object {

    fun create(appUpdateNudgePriority: AppUpdateNudgePriority) = CriticalAppUpdateModel(
        appUpdateHelpContact = Optional.empty(),
        appStaleness = null,
        appUpdateNudgePriority = appUpdateNudgePriority
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
