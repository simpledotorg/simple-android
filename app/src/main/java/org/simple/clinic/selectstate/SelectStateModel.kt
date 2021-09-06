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

  val hasStates
    get() = states != null

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
}
