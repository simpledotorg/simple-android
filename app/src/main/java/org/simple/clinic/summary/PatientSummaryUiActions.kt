package org.simple.clinic.summary

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface PatientSummaryUiActions {
  fun showScheduleAppointmentSheet(patientUuid: UUID, sheetOpenedFrom: AppointmentSheetOpenedFrom)
  fun showEditPatientScreen(patientSummaryProfile: PatientSummaryProfile)
  fun goToPreviousScreen()
  fun goToHomeScreen()
  fun showUpdatePhoneDialog(patientUuid: UUID)
  fun showAddPhoneDialog(patientUuid: UUID)
  fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier)
  fun hideLinkIdWithPatientView()
}
