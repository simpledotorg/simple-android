package org.simple.clinic.selectstate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appconfig.State

@Parcelize
data class SelectStateModel(
    val states: List<State>?,
    val statesFetchError: StatesFetchError?,
    val selectedState: State?
) : Parcelable {

  val hasMoreThanOneState: Boolean
    get() = states.orEmpty().size > 1

  val hasFetchError
    get() = statesFetchError != null

  val hasSelectedState
    get() = selectedState != null

  val hasStates
    get() = states != null

  val isFetching
    get() = states == null && statesFetchError == null

  companion object {

    fun create() = SelectStateModel(
        states = null,
        statesFetchError = null,
        selectedState = null
    )
  }

  fun statesLoaded(states: List<State>) = copy(states = states, statesFetchError = null)

  fun failedToLoadStates(statesFetchError: StatesFetchError) = copy(statesFetchError = statesFetchError, states = null)

  fun stateChanged(state: State) = copy(selectedState = state)

  fun loadingStates() = copy(states = null, statesFetchError = null)
}
