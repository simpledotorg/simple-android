package org.simple.clinic.selectstate

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
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
      StateSaved -> stateSaved(model)
      is StatesFetched -> statesFetched(model, event.states)
      is FailedToFetchStates -> next(model.failedToLoadStates(event.error))
      RetryButtonClicked -> next(
          model.loadingStates(),
          LoadStates
      )
      is StateChanged -> next(model.stateChanged(event.state), SaveSelectedState(event.state))
    }
  }

  private fun stateSaved(model: SelectStateModel): Next<SelectStateModel, SelectStateEffect> {
    val effect = if (model.hasMoreThanOneState) {
      GoToRegistrationScreen
    } else {
      ReplaceCurrentScreenWithRegistrationScreen
    }

    return dispatch(effect)
  }

  private fun statesFetched(model: SelectStateModel, states: List<State>): Next<SelectStateModel, SelectStateEffect> {
    val statesLoadedModel = model.statesLoaded(states)

    return if (states.size > 1) {
      next(statesLoadedModel)
    } else {
      next(statesLoadedModel, SaveSelectedState(states.first()))
    }
  }
}
