package org.simple.clinic.home.overdue.phonemask

import android.Manifest
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RequiresPermission
import org.simple.clinic.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class PhoneMaskBottomSheetCreated(val patientUuid: UUID) : UiEvent

data class NormalCallClicked(
    override var permission: Optional<RuntimePermissionResult> = None,
    override val permissionRequestCode: Int = 1,
    override val permissionString: String = Manifest.permission.CALL_PHONE
) : UiEvent, RequiresPermission {
  override val analyticsName = "Phone mask bottom sheet:Normal call clicked"
}

data class SecureCallClicked(
    override var permission: Optional<RuntimePermissionResult> = None,
    override val permissionRequestCode: Int = 2,
    override val permissionString: String = Manifest.permission.CALL_PHONE
) : UiEvent, RequiresPermission {
  override val analyticsName = "Phone mask bottom sheet:Secure call clicked"
}
