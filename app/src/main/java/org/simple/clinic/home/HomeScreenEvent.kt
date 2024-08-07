package org.simple.clinic.home

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Optional
import org.simple.clinic.activity.permissions.RequiresPermission
import org.simple.clinic.facility.Facility
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent

sealed class HomeScreenEvent : UiEvent

data object HomeFacilitySelectionClicked : HomeScreenEvent() {
  override val analyticsName = "Home Screen:Facility Clicked"
}

data class CurrentFacilityLoaded(val facility: Facility) : HomeScreenEvent()

data class OverdueAppointmentCountUpdated(val overdueAppointmentCount: Int) : HomeScreenEvent()

data class RequestNotificationPermission(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String,
    override val permissionRequestCode: Int = 1
) : HomeScreenEvent(), RequiresPermission
