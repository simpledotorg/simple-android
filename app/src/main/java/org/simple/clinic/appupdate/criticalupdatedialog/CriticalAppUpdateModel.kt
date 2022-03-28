package org.simple.clinic.appupdate.criticalupdatedialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appupdate.AppUpdateHelpContact
import org.simple.clinic.appupdate.AppUpdateNudgePriority
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL
import org.simple.clinic.appupdate.AppUpdateNudgePriority.CRITICAL_SECURITY
import org.simple.clinic.util.ParcelableOptional
import org.simple.clinic.util.parcelable
import java.util.Optional

@Parcelize
data class CriticalAppUpdateModel(
    val appUpdateHelpContact: ParcelableOptional<AppUpdateHelpContact>?,
    val appStaleness: Int?,
    val appUpdateNudgePriority: AppUpdateNudgePriority
) : Parcelable {

  companion object {

    fun create(appUpdateNudgePriority: AppUpdateNudgePriority) = CriticalAppUpdateModel(
        appUpdateHelpContact = null,
        appStaleness = null,
        appUpdateNudgePriority = appUpdateNudgePriority
    )
  }

  val contactUrl: String
    get() = appUpdateHelpContact!!.get().url

  val hasHelpContact: Boolean
    get() = appUpdateHelpContact != null && appUpdateHelpContact.isPresent()

  val hasAppStaleness: Boolean
    get() = appStaleness != null

  val isCriticalSecurityUpdateNudgePriority: Boolean
    get() = appUpdateNudgePriority == CRITICAL_SECURITY

  val isCriticalUpdateNudgePriority: Boolean
    get() = appUpdateNudgePriority == CRITICAL

  fun appUpdateHelpContactLoaded(appUpdateHelpContact: Optional<AppUpdateHelpContact>): CriticalAppUpdateModel {
    return copy(appUpdateHelpContact = appUpdateHelpContact.parcelable())
  }

  fun appStalenessLoaded(appStaleness: Int): CriticalAppUpdateModel {
    return copy(appStaleness = appStaleness)
  }
}
