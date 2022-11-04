package org.simple.clinic.home.patients

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appupdate.AppUpdateNudgePriority
import org.simple.clinic.appupdate.AppUpdateNudgePriority.MEDIUM
import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User
import org.simple.clinic.util.toNullable
import java.util.Optional

@Parcelize
data class PatientsTabModel(
    val user: User?,
    val numberOfPatientsRegistered: Int?,
    val appStaleness: Int?,
    val appUpdateNudgePriority: AppUpdateNudgePriority?,
    val isDrugStockReportFilled: Boolean?,
    val facility: Facility?
) : Parcelable {

  companion object {

    fun create(): PatientsTabModel = PatientsTabModel(
        user = null,
        numberOfPatientsRegistered = null,
        appStaleness = null,
        appUpdateNudgePriority = null,
        isDrugStockReportFilled = null,
        facility = null
    )
  }

  val hasFacility: Boolean
    get() = facility != null

  val hasLoadedUser: Boolean
    get() = user != null

  val hasAppStaleness
    get() = appStaleness != null

  val hasAppUpdateNudgePriority
    get() = appUpdateNudgePriority != null

  val appUpdateNudgePriorityIsMedium
    get() = hasAppUpdateNudgePriority && appUpdateNudgePriority == MEDIUM

  fun userLoaded(user: User): PatientsTabModel {
    return copy(user = user)
  }

  fun updateAppStaleness(appStaleness: Int?): PatientsTabModel {
    return copy(appStaleness = appStaleness)
  }

  fun appUpdateNudgePriorityUpdated(appUpdateNudgePriority: AppUpdateNudgePriority?): PatientsTabModel {
    return copy(appUpdateNudgePriority = appUpdateNudgePriority)
  }

  fun updateIsDrugStockFilled(isDrugStockReportFilled: Optional<Boolean>): PatientsTabModel {
    return copy(isDrugStockReportFilled = isDrugStockReportFilled.toNullable())
  }

  fun currentFacilityLoaded(facility: Facility): PatientsTabModel {
    return copy(facility = facility)
  }
}
