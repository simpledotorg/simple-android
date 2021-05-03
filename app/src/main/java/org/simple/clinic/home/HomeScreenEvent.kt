package org.simple.clinic.home

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.scanid.SearchByEnteredCode
import org.simple.clinic.scanid.PatientFound
import org.simple.clinic.scanid.PatientNotFound
import org.simple.clinic.scanid.ScanResult
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

sealed class HomeScreenEvent : UiEvent

object HomeFacilitySelectionClicked : HomeScreenEvent() {
  override val analyticsName = "Home Screen:Facility Clicked"
}

data class CurrentFacilityLoaded(val facility: Facility) : HomeScreenEvent()

data class OverdueAppointmentCountLoaded(val overdueAppointmentCount: Int) : HomeScreenEvent()

sealed class BusinessIdScanned : HomeScreenEvent() {

  companion object {
    fun fromScanResult(scanResult: ScanResult): BusinessIdScanned {
      return when (scanResult) {
        is SearchByEnteredCode -> ByShortCode(scanResult.shortCode)
        is PatientFound -> ByPatientFound(scanResult.patientId)
        is PatientNotFound -> ByPatientNotFound(scanResult.identifier)
      }
    }
  }

  data class ByShortCode(val shortCode: String) : BusinessIdScanned()

  data class ByPatientFound(val patientId: UUID) : BusinessIdScanned()

  data class ByPatientNotFound(val identifier: Identifier) : BusinessIdScanned()
}

