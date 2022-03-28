package org.simple.clinic.home.patients

import android.Manifest
import org.simple.clinic.activity.permissions.RequiresPermission
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import java.time.LocalDate
import java.util.Optional

sealed class PatientsTabEvent : UiEvent

class PatientsEnterCodeManuallyClicked : PatientsTabEvent() {
  override val analyticsName = "Patients:Enter Code Manually Clicked"
}

object NewPatientClicked : PatientsTabEvent() {
  override val analyticsName = "Patients:Search For Patient Clicked"
}

data class UserDetailsLoaded(val user: User) : PatientsTabEvent()

object ActivityResumed : PatientsTabEvent()

data class DataForShowingApprovedStatusLoaded(
    val currentTime: Instant,
    val approvalStatusUpdatedAt: Instant,
    val hasBeenDismissed: Boolean
) : PatientsTabEvent()

class UserApprovedStatusDismissed : PatientsTabEvent() {
  override val analyticsName = "Patients:Dismissed User Approved Status"
}

data class ScanCardIdButtonClicked(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String = Manifest.permission.CAMERA,
    override val permissionRequestCode: Int = 1
) : PatientsTabEvent(), RequiresPermission {

  override val analyticsName: String = "Patients:Scan Simple Card Clicked:"
}

data class LoadedNumberOfPatientsRegistered(val numberOfPatientsRegistered: Int) : PatientsTabEvent()

object SimpleVideoClicked : PatientsTabEvent() {
  override val analyticsName = "Patients:Simple Video Clicked"
}

data class RequiredInfoForShowingAppUpdateLoaded(
    val isAppUpdateAvailable: Boolean,
    val appUpdateLastShownOn: LocalDate,
    val currentDate: LocalDate
) : PatientsTabEvent()

data class AppStalenessLoaded(val appStaleness: Int) : PatientsTabEvent()
