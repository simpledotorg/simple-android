package org.simple.clinic.home

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.scanid.EnteredShortCode
import org.simple.clinic.scanid.PatientFound
import org.simple.clinic.scanid.ScanResult
import org.simple.clinic.scanid.ScannedId
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class HomeScreenEvent : UiEvent

object HomeFacilitySelectionClicked : HomeScreenEvent() {
  override val analyticsName = "Home Screen:Facility Clicked"
}

data class CurrentFacilityLoaded(val facility: Facility) : HomeScreenEvent()

data class OverdueAppointmentCountLoaded(val overdueAppointmentCount: Int) : HomeScreenEvent()

data class PatientSearchByIdentifierCompleted(val patient: Optional<Patient>, val identifier: Identifier) : HomeScreenEvent()

sealed class BusinessIdScanned : HomeScreenEvent() {

  companion object {
    fun fromScanResult(scanResult: ScanResult): BusinessIdScanned {
      return when (scanResult) {
        is ScannedId -> ByIdentifier(scanResult.identifier)
        is EnteredShortCode -> ByShortCode(scanResult.shortCode)
        is PatientFound -> ByPatientFound(scanResult.patientId)
      }
    }
  }

  data class ByIdentifier(val identifier: Identifier) : BusinessIdScanned()

  data class ByShortCode(val shortCode: String) : BusinessIdScanned()

  data class ByPatientFound(val patientId: UUID) : BusinessIdScanned()
}

