package org.simple.clinic.summary

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier
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
  fun showDiagnosisError()
  fun openPatientContactSheet(patientUuid: UUID)
  fun navigateToTeleconsultRecordScreen(patientUuid: UUID, teleconsultRecordId: UUID)
  fun openContactDoctorSheet(patientUuid: UUID)
}
