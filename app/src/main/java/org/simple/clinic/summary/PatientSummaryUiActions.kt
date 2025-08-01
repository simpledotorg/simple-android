package org.simple.clinic.summary

import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.reassignpatient.ReassignPatientSheetOpenedFrom
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
  fun showAddMeasurementsWarningDialog()
  fun showAddBloodPressureWarningDialog()
  fun showAddBloodSugarWarningDialog()
  fun openSelectFacilitySheet()
  fun dispatchNewAssignedFacility(facility: Facility)
  fun refreshNextAppointment()
  fun showReassignPatientWarningSheet(
      patientUuid: UUID,
      currentFacility: Facility,
      sheetOpenedFrom: ReassignPatientSheetOpenedFrom
  )

  fun showDiabetesDiagnosisWarning()
  fun showHypertensionDiagnosisWarning(continueToDiabetesDiagnosisWarning: Boolean)
  fun showTobaccoStatusDialog()
  fun openBMIEntrySheet(patientUuid: UUID)
  fun openCholesterolEntrySheet(patientUuid: UUID)
}
