package org.simple.clinic.home.overdue

import android.Manifest
import androidx.paging.PagingData
import org.simple.clinic.activity.permissions.RequiresPermission
import org.simple.clinic.analytics.NetworkConnectivityStatus
import org.simple.clinic.facility.Facility
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.widgets.UiEvent
import java.util.Optional
import java.util.UUID

sealed class OverdueEvent : UiEvent

data class CurrentFacilityLoaded(val facility: Facility) : OverdueEvent()

data class CallPatientClicked(val patientUuid: UUID) : OverdueEvent() {
  override val analyticsName = "Overdue Screen:Call Patient clicked"
}

data class OverduePatientClicked(val patientUuid: UUID) : OverdueEvent() {
  override val analyticsName = "Overdue Screen:Patient name clicked"
}

data class OverdueAppointmentsLoaded(
    val overdueAppointments: PagingData<OverdueAppointment>
) : OverdueEvent()

data class DownloadOverdueListClicked(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String = Manifest.permission.WRITE_EXTERNAL_STORAGE,
    override val permissionRequestCode: Int = 1
) : OverdueEvent(), RequiresPermission {

  override val analyticsName = "Overdue Screen:Download clicked"
}

object ShareOverdueListClicked : OverdueEvent() {

  override val analyticsName = "Overdue Screen:Share clicked"
}

data class NetworkConnectivityStatusLoaded(val status: NetworkConnectivityStatus) : OverdueEvent()
