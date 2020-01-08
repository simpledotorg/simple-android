package org.simple.clinic.summary

import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.summary.prescribeddrugs.DrugSummaryUi
import java.util.UUID

interface PatientSummaryScreenUi {
  fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile)
  fun showScheduleAppointmentSheet(patientUuid: UUID)
  fun goToPreviousScreen()
  fun goToHomeScreen()
  fun showUpdatePhoneDialog(patientUuid: UUID)
  fun showAddPhoneDialog(patientUuid: UUID)
  fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier)
  fun hideLinkIdWithPatientView()
  fun showEditButton()
  fun drugSummaryUi(): DrugSummaryUi
}
