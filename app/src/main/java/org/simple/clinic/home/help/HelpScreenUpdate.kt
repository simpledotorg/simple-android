package org.simple.clinic.home.help

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class HelpScreenUpdate : Update<HelpScreenModel, HelpScreenEvent, HelpScreenEffect> {
  override fun update(model: HelpScreenModel, event: HelpScreenEvent): Next<HelpScreenModel, HelpScreenEffect> {
    return noChange()
  }
}
