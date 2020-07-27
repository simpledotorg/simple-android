package org.simple.clinic.facilitypicker

import org.simple.clinic.facility.change.FacilityListItem

interface FacilityPickerUi {
  fun showProgressIndicator()
  fun hideProgressIndicator()
  fun showToolbarWithSearchField()
  fun showToolbarWithoutSearchField()
  fun updateFacilities(facilityItems: List<FacilityListItem>)
}
