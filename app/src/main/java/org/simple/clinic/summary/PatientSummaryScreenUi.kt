package org.simple.clinic.summary

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface PatientSummaryScreenUi : PatientSummaryUiActions {
  fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile)
  fun showEditButton()
  fun showDiabetesView()
  fun hideDiabetesView()

  // Not yet migrated to Mobius
  fun showAddPhoneDialog(patientUuid: UUID)
  fun showLinkIdWithPatientView(patientUuid: UUID, identifier: Identifier)
  fun hideLinkIdWithPatientView()
}
