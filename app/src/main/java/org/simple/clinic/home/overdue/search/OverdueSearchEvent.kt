package org.simple.clinic.home.overdue.search

import android.Manifest
import androidx.paging.PagingData
import org.simple.clinic.activity.permissions.RequiresPermission
import org.simple.clinic.analytics.NetworkConnectivityStatus
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.util.RequiresNetwork
import org.simple.clinic.widgets.UiEvent
import java.util.Optional
import java.util.UUID

sealed class OverdueSearchEvent : UiEvent

data class OverdueSearchResultsLoaded(val overdueAppointments: PagingData<OverdueAppointment>) : OverdueSearchEvent()

data class OverduePatientClicked(val patientUuid: UUID) : OverdueSearchEvent()

data class CallPatientClicked(val patientUuid: UUID) : OverdueSearchEvent()

data class OverdueSearchLoadStateChanged(val overdueSearchProgressState: OverdueSearchProgressState) : OverdueSearchEvent()

data class OverdueAppointmentCheckBoxClicked(val appointmentId: UUID) : OverdueSearchEvent()

object ClearSelectedOverdueAppointmentsClicked : OverdueSearchEvent()

data class SelectedOverdueAppointmentsLoaded(val selectedAppointmentIds: Set<UUID>) : OverdueSearchEvent()

data class SelectedAppointmentIdsReplaced(val type: OverdueButtonType) : OverdueSearchEvent()

data class DownloadButtonClicked(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String = Manifest.permission.WRITE_EXTERNAL_STORAGE,
    override val permissionRequestCode: Int = 2,
    override var networkStatus: Optional<NetworkConnectivityStatus> = Optional.empty()
) : OverdueSearchEvent(), RequiresPermission, RequiresNetwork {

  override val analyticsName = "Overdue Search Screen:Download Clicked"
}

data class ShareButtonClicked(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String = Manifest.permission.WRITE_EXTERNAL_STORAGE,
    override val permissionRequestCode: Int = 2,
    override var networkStatus: Optional<NetworkConnectivityStatus> = Optional.empty()
) : OverdueSearchEvent(), RequiresPermission, RequiresNetwork {

  override val analyticsName = "Overdue Search Screen:Share Clicked"
}

object SelectAllButtonClicked : OverdueSearchEvent() {
  override val analyticsName = "Overdue Search Screen:Select All Clicked"
}

data class SearchResultsAppointmentIdsLoaded(
    val buttonType: OverdueButtonType,
    val searchResultsAppointmentIds: Set<UUID>
) : OverdueSearchEvent()

data class VillagesAndPatientNamesLoaded(
    val villagesAndPatientNames: List<String>
) : OverdueSearchEvent()

data class OverdueSearchInputsChanged(val searchInputs: List<String>) : OverdueSearchEvent()
