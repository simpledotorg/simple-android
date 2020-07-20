package org.simple.clinic.registration.facility

import org.simple.clinic.facility.change.FacilityListItem

interface RegistrationFacilitySelectionUi {
  fun showProgressIndicator()
  fun hideProgressIndicator()
  fun showToolbarWithSearchField()
  fun showToolbarWithoutSearchField()
  fun updateFacilities(facilityItems: List<FacilityListItem>)
}
