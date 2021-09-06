package org.simple.clinic.selectstate

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class SelectStateUpdate : Update<SelectStateModel, SelectStateEvent, SelectStateEffect> {

  override fun update(
      model: SelectStateModel,
      event: SelectStateEvent
  ): Next<SelectStateModel, SelectStateEffect> {
    return when (event) {
      StateSaved -> noChange()
      is StatesFetched -> next(model.statesLoaded(event.states))
      is FailedToFetchStates -> next(model.failedToLoadStates(event.error))
    }
  }
}
