package org.simple.clinic.instantsearch

import android.Manifest
import org.simple.clinic.bp.assignbppassport.BlankBpPassportResult
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.scanid.EnteredShortCode
import org.simple.clinic.scanid.PatientFound
import org.simple.clinic.scanid.PatientNotFound
import org.simple.clinic.scanid.ScanResult
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RequiresPermission
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class InstantSearchEvent : UiEvent

data class CurrentFacilityLoaded(val facility: Facility) : InstantSearchEvent()

data class AllPatientsLoaded(val patients: List<PatientSearchResult>) : InstantSearchEvent()

data class SearchResultsLoaded(val patientsSearchResults: List<PatientSearchResult>) : InstantSearchEvent()

data class SearchQueryValidated(val result: InstantSearchValidator.Result) : InstantSearchEvent()

data class SearchResultClicked(val patientId: UUID) : InstantSearchEvent() {

  override val analyticsName: String = "Instant Search: Search Result Clicked"
}

data class SearchQueryChanged(val searchQuery: String) : InstantSearchEvent()

object SavedNewOngoingPatientEntry : InstantSearchEvent()

object RegisterNewPatientClicked : InstantSearchEvent() {

  override val analyticsName: String = "Instant Search: Register New Patient"
}

data class BlankBpPassportResultReceived(val blankBpPassportResult: BlankBpPassportResult) : InstantSearchEvent() {

  override val analyticsName: String = "Instant Search : Blank BP Passport Result Received"
}

sealed class BpPassportScanned : InstantSearchEvent() {

  companion object {

    fun fromResult(scanResult: ScanResult): BpPassportScanned {
      return when (scanResult) {
        is EnteredShortCode -> ByShortCode(scanResult.shortCode)
        is PatientFound -> ByPatientFound(scanResult.patientId)
        is PatientNotFound -> ByPatientNotFound(scanResult.identifier)
      }
    }
  }

  data class ByPatientFound(val patientId: UUID) : BpPassportScanned()

  data class ByPatientNotFound(val identifier: Identifier) : BpPassportScanned()

  data class ByShortCode(val shortCode: String) : BpPassportScanned()
}

data class OpenQrCodeScannerClicked(
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String = Manifest.permission.CAMERA,
    override val permissionRequestCode: Int = 1
) : InstantSearchEvent(), RequiresPermission {
  override val analyticsName = "Instant Search Screen:Open QR code scanner"
}
