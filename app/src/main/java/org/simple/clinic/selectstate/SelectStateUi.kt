package org.simple.clinic.selectstate

import org.simple.clinic.appconfig.State

interface SelectStateUi {
  fun showStates(states: List<State>, selectedState: State?)
  fun hideStates()
  fun showNetworkErrorMessage()
  fun showServerErrorMessage()
  fun showGenericErrorMessage()
  fun hideErrorView()
  fun showProgress()
  fun hideProgress()
}
