package org.simple.clinic.home.patients

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appupdate.AppUpdateNudgePriority
import org.simple.clinic.user.User

@Parcelize
data class PatientsTabModel(
    val user: User?,
    val numberOfPatientsRegistered: Int?,
    val appStaleness: Int?,
    val appUpdateNudgePriority: AppUpdateNudgePriority?
) : Parcelable {

  companion object {

    fun create(): PatientsTabModel = PatientsTabModel(
        user = null,
        numberOfPatientsRegistered = null,
        appStaleness = null,
        appUpdateNudgePriority = null
    )
  }

  val hasLoadedUser: Boolean
    get() = user != null

  val hasLoadedNumberOfPatientsRegistered: Boolean
    get() = numberOfPatientsRegistered != null

  val hasAppStaleness
    get() = appStaleness != null

  val hasAppUpdateNudgePriority
    get() = appUpdateNudgePriority != null

  fun userLoaded(user: User): PatientsTabModel {
    return copy(user = user)
  }

  fun numberOfPatientsRegisteredUpdated(numberOfPatientsRegistered: Int): PatientsTabModel {
    return copy(numberOfPatientsRegistered = numberOfPatientsRegistered)
  }

  fun updateAppStaleness(appStaleness: Int): PatientsTabModel {
    return copy(appStaleness = appStaleness)
  }

  fun appUpdateNudgePriorityUpdated(appUpdateNudgePriority: AppUpdateNudgePriority): PatientsTabModel {
    return copy(appUpdateNudgePriority = appUpdateNudgePriority)
  }
}
