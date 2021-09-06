package org.simple.clinic.selectstate

import org.simple.clinic.appconfig.State

interface SelectStateUi {
  fun showStates(states: List<State>, selectedState: State?)
  fun showNextButton()
  fun hideStates()
}
