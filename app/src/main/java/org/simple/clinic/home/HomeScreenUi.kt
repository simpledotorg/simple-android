package org.simple.clinic.home

interface HomeScreenUi : HomeScreenUiActions {
  fun setFacility(facilityName: String)
  fun showOverdueAppointmentCount(count: Int)
  fun removeOverdueAppointmentCount()
}
