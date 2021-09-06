package org.simple.clinic.selectstate

import org.simple.clinic.appconfig.State
import org.simple.clinic.appconfig.StatesResult

sealed class SelectStateEvent

data class StatesResultFetched(val result: StatesResult) : SelectStateEvent()

object StateSaved : SelectStateEvent()

data class StatesFetched(val states: List<State>) : SelectStateEvent()

data class FailedToFetchStates(val error: StatesFetchError) : SelectStateEvent()
