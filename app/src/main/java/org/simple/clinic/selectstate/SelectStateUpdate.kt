package org.simple.clinic.selectstate

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.appconfig.StatesResult
import org.simple.clinic.mobius.next

class SelectStateUpdate : Update<SelectStateModel, SelectStateEvent, SelectStateEffect> {

  override fun update(
      model: SelectStateModel,
      event: SelectStateEvent
  ): Next<SelectStateModel, SelectStateEffect> {
    return when (event) {
      is StatesResultFetched -> statesResultFetched(model, event)
    }
  }

  private fun statesResultFetched(
      model: SelectStateModel,
      event: StatesResultFetched
  ): Next<SelectStateModel, SelectStateEffect> {
    val updatedModel = when (val result = event.result) {
      is StatesResult.FetchError -> model.failedToLoadStates(StatesFetchError.fromResolvedError(result.error))
      is StatesResult.StatesFetched -> model.statesLoaded(result.states)
    }

    return next(updatedModel)
  }
}
