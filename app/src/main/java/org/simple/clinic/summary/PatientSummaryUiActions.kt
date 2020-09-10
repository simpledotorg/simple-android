package org.simple.clinic.summary

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.summary.teleconsultation.api.TeleconsultPhoneNumber
import java.util.UUID

interface PatientSummaryUiActions {
  fun showScheduleAppointmentSheet(
      patientUuid: UUID,
      sheetOpenedFrom: AppointmentSheetOpenedFrom,
      currentFacility: Facility
  )
  fun showEditPatientScreen(
      patientSummaryProfile: PatientSummaryProfile,
      currentFacility: Facility
  )
  fun goToPreviousScreen()
  fun goToHomeScreen()
  fun showUpdatePhoneDialog(patientUuid: UUID)
  fun showAddPhoneDialog(patientUuid: UUID)
  fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier)
  fun hideLinkIdWithPatientView()
  fun showDiagnosisError()
  fun openPatientContactSheet(patientUuid: UUID)
  fun contactDoctor(patientTeleconsultationInfo: PatientTeleconsultationInfo, teleconsultationPhoneNumber: String)
  fun showTeleconsultInfoError()
  fun openContactDoctorSheet(
      facility: Facility,
      phoneNumbers: List<TeleconsultPhoneNumber>
  )
  fun navigateToTeleconsultRecordScreen(patientUuid: UUID, teleconsultRecordId: UUID)
}
