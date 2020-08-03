package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility

interface FacilityChangeUiActions {
  fun openConfirmationSheet(facility: Facility)
  fun goBack()
}
