package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility

interface FacilityChangeUi: FacilityChangeUiActions {
  fun goBack()
  fun openConfirmationSheet(facility: Facility)
}
