package org.simple.clinic.registration.location

import org.simple.clinic.location.LOCATION_PERMISSION
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RequiresPermission
import org.simple.clinic.widgets.UiEvent

sealed class RegistrationLocationPermissionEvent : UiEvent

data class RequestLocationPermission(
    override var permission: Optional<RuntimePermissionResult> = None(),
    override val permissionRequestCode: Int = 1,
    override val permissionString: String = LOCATION_PERMISSION
) : RegistrationLocationPermissionEvent(), RequiresPermission {
  override val analyticsName = "Registration:Location Access Clicked"
}

object SkipClicked : RegistrationLocationPermissionEvent() {
  override val analyticsName: String = "Registration:Location Permission:Skip Clicked"
}
