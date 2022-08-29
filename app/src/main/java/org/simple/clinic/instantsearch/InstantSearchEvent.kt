package org.simple.clinic.instantsearch

import android.Manifest
import androidx.paging.PagingData
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.scanid.scannedqrcode.BlankScannedQRCodeResult
import org.simple.clinic.activity.permissions.RequiresPermission
import org.simple.clinic.widgets.UiEvent
import java.util.Optional
import java.util.UUID

sealed class InstantSearchEvent : UiEvent

data class CurrentFacilityLoaded(val facility: Facility) : InstantSearchEvent()

data class AllPatientsInFacilityLoaded(val patients: PagingData<PatientSearchResult>) : InstantSearchEvent()

data class SearchResultsLoaded(val patientsSearchResults: PagingData<PatientSearchResult>) : InstantSearchEvent()

data class SearchQueryValidated(val result: InstantSearchValidator.Result) : InstantSearchEvent()

data class SearchResultClicked(val patientId: UUID) : InstantSearchEvent() {

  override val analyticsName: String = "Instant Search: Search Result Clicked"
}

data class SearchQueryChanged(val searchQuery: String) : InstantSearchEvent()

object SavedNewOngoingPatientEntry : InstantSearchEvent()

object RegisterNewPatientClicked : InstantSearchEvent() {

  override val analyticsName: String = "Instant Search: Register New Patient"
}

data class BlankScannedQrCodeResultReceived(val blankScannedQRCodeResult: BlankScannedQRCodeResult) : InstantSearchEvent() {

  override val analyticsName: String = "Instant Search : Blank BP Passport Result Received"
}

data class OpenQrCodeScannerClicked(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String = Manifest.permission.CAMERA,
    override val permissionRequestCode: Int = 1
) : InstantSearchEvent(), RequiresPermission {
  override val analyticsName = "Instant Search Screen:Open QR code scanner"
}

object PatientAlreadyHasAnExistingNHID : InstantSearchEvent()

data class PatientDoesNotHaveAnExistingNHID(val patientId: UUID) : InstantSearchEvent()

data class SearchResultsLoadStateChanged(val instantSearchProgressState: InstantSearchProgressState) : InstantSearchEvent()

object InstantSearchScreenShown : InstantSearchEvent()
