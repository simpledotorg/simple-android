package org.simple.clinic.selectstate

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.appconfig.State
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class SelectStateUpdate : Update<SelectStateModel, SelectStateEvent, SelectStateEffect> {

  override fun update(
      model: SelectStateModel,
      event: SelectStateEvent
  ): Next<SelectStateModel, SelectStateEffect> {
    return when (event) {
      StateSaved -> dispatch(GoToRegistrationScreen)
      is StatesFetched -> statesFetched(model, event.states)
      is FailedToFetchStates -> next(model.failedToLoadStates(event.error))
      RetryButtonClicked -> next(
          model.loadingStates(),
          LoadStates
      )
      is StateChanged -> next(model.stateChanged(event.state), SaveSelectedState(event.state))
    }
  }

  private fun statesFetched(model: SelectStateModel, states: List<State>): Next<SelectStateModel, SelectStateEffect> {
    return if (states.size > 1) {
      next(model.statesLoaded(states))
    } else {
      dispatch(SaveSelectedState(states.first()))
    }
  }
}
