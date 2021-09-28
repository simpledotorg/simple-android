package org.simple.clinic.selectstate

import org.simple.clinic.appconfig.State
import org.simple.clinic.widgets.UiEvent

sealed class SelectStateEvent : UiEvent

object StateSaved : SelectStateEvent()

data class StatesFetched(val states: List<State>) : SelectStateEvent()

data class FailedToFetchStates(val error: StatesFetchError) : SelectStateEvent()

object RetryButtonClicked : SelectStateEvent() {
  override val analyticsName: String = "Select State:Retry Clicked"
}

data class StateChanged(val state: State) : SelectStateEvent() {
  override val analyticsName: String = "Select State:State Changed:${state.displayName}"
}
