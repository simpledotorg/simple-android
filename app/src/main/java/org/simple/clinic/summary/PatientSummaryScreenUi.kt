package org.simple.clinic.summary

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface PatientSummaryScreenUi {
  fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile)

  fun populateList(prescribedDrugs: List<PrescribedDrug>, medicalHistory: MedicalHistory)

  fun showBloodPressureUpdateSheet(bloodPressureMeasurementUuid: UUID)
  fun showScheduleAppointmentSheet(patientUuid: UUID)
  fun goToPreviousScreen()
  fun goToHomeScreen()
  fun showUpdatePhoneDialog(patientUuid: UUID)
  fun showAddPhoneDialog(patientUuid: UUID)
  fun showUpdatePrescribedDrugsScreen(patientUuid: UUID)
  fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier)
  fun hideLinkIdWithPatientView()
  fun showEditButton()
}
