package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility

interface FacilityChangeUi {
  fun goBack()
  fun openConfirmationSheet(facility: Facility)
}
