package org.simple.clinic.home.patients.links

import android.Manifest
import org.simple.clinic.activity.permissions.RequiresPermission
import org.simple.clinic.facility.Facility
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import java.util.Optional

sealed class PatientsTabLinkEvent : UiEvent

data class CurrentFacilityLoaded(
    val facility: Facility
) : PatientsTabLinkEvent()

object MonthlyScreeningReportsClicked : PatientsTabLinkEvent() {
  override val analyticsName = "Link: Monthly Screening Report clicked"
}

data class DownloadPatientLineListClicked(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String = Manifest.permission.WRITE_EXTERNAL_STORAGE,
    override val permissionRequestCode: Int = 1
) : PatientsTabLinkEvent(), RequiresPermission {
  override val analyticsName = "Link: Download Patient Line List clicked"
}
