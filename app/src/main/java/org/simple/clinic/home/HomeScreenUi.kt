package org.simple.clinic.home

interface HomeScreenUi : HomeScreenUiActions {
  fun setFacility(facilityName: String)
  fun openFacilitySelection()
  fun showOverdueAppointmentCount(count: Int)
  fun removeOverdueAppointmentCount()
}
