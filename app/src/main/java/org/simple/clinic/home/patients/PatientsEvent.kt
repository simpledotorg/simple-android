package org.simple.clinic.home.patients

import android.Manifest
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.user.User
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RequiresPermission
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant

sealed class PatientsEvent : UiEvent

class PatientsEnterCodeManuallyClicked : PatientsEvent() {
  override val analyticsName = "Patients:Enter Code Manually Clicked"
}

object NewPatientClicked : PatientsEvent() {
  override val analyticsName = "Patients:Search For Patient Clicked"
}

data class UserDetailsLoaded(val user: User) : PatientsEvent()

object ActivityResumed : PatientsEvent()

data class DataForShowingApprovedStatusLoaded(
    val currentTime: Instant,
    val approvalStatusUpdatedAt: Instant,
    val hasBeenDismissed: Boolean
) : PatientsEvent()

class UserApprovedStatusDismissed : PatientsEvent() {
  override val analyticsName = "Patients:Dismissed User Approved Status"
}

data class ScanCardIdButtonClicked(
    override var permission: Optional<RuntimePermissionResult> = None,
    override val permissionString: String = Manifest.permission.CAMERA,
    override val permissionRequestCode: Int = 1
) : PatientsEvent(), RequiresPermission {

  override val analyticsName: String = "Patients:Scan Simple Card Clicked:"
}

data class LoadedNumberOfPatientsRegistered(val numberOfPatientsRegistered: Int): PatientsEvent()

object SimpleVideoClicked : PatientsEvent() {
  override val analyticsName = "Patients:Simple Video Clicked"
}

