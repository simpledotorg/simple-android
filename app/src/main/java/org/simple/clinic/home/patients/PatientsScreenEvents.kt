package org.simple.clinic.home.patients

import android.Manifest
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RequiresPermission
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent

class UserApprovedStatusDismissed : UiEvent {
  override val analyticsName = "Patients:Dismissed User Approved Status"
}

data class ScanCardIdButtonClicked(
    override var permission: Optional<RuntimePermissionResult> = None,
    override val permissionString: String = Manifest.permission.CAMERA,
    override val permissionRequestCode: Int = 1
) : UiEvent, RequiresPermission {

  override val analyticsName: String = "Patients:Scan Simple Card Clicked:"
}

object SimpleVideoClicked : UiEvent {
  override val analyticsName = "Patients:Simple Video Clicked"
}
