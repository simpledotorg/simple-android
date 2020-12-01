package org.simple.clinic.home

import org.simple.clinic.patient.businessid.Identifier

interface HomeScreenUiActions {
  fun openFacilitySelection()
  fun openShortCodeSearchScreen(shortCode: String)
  fun openPatientSearchScreen(additionalIdentifier: Identifier?)
}
