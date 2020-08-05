package org.simple.clinic.home.help

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class HelpScreenUpdate : Update<HelpScreenModel, HelpScreenEvent, HelpScreenEffect> {
  override fun update(model: HelpScreenModel, event: HelpScreenEvent): Next<HelpScreenModel, HelpScreenEffect> {
    return when (event) {
      HelpScreenTryAgainClicked -> dispatch(ShowLoadingView)
    }
  }
}
