package org.simple.clinic.facilitypicker

import org.simple.clinic.facility.Facility

interface FacilityPickerUiActions {
  fun dispatchSelectedFacility(facility: Facility)
}
