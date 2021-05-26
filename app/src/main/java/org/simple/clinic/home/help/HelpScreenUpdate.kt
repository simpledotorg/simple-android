package org.simple.clinic.home.help

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.util.toNullable

class HelpScreenUpdate : Update<HelpScreenModel, HelpScreenEvent, HelpScreenEffect> {
  override fun update(
      model: HelpScreenModel,
      event: HelpScreenEvent
  ): Next<HelpScreenModel, HelpScreenEffect> {
    return when (event) {
      HelpScreenTryAgainClicked -> dispatch(ShowLoadingView, SyncHelp)
      is HelpContentLoaded -> next(model.helpContentLoaded(event.helpContent.toNullable()))
      is HelpSyncPullResult -> next(model.helpPullResultUpdated(event.result))
    }
  }
}
