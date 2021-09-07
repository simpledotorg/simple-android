package org.simple.clinic.selectstate

import org.simple.clinic.appconfig.State

sealed class SelectStateEvent

object StateSaved : SelectStateEvent()

data class StatesFetched(val states: List<State>) : SelectStateEvent()

data class FailedToFetchStates(val error: StatesFetchError) : SelectStateEvent()

object RetryButtonClicked : SelectStateEvent()

data class StateChanged(val state: State) : SelectStateEvent()

object NextClicked : SelectStateEvent()
