package org.simple.clinic.home

import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

interface HomeScreenUiActions {
  fun openFacilitySelection()
  fun openShortCodeSearchScreen(shortCode: String)
  fun openPatientSearchScreen(additionalIdentifier: Identifier?)
  fun openPatientSummary(patientId: UUID)
}
