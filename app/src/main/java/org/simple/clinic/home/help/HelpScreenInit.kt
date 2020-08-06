package org.simple.clinic.home.help

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class HelpScreenInit : Init<HelpScreenModel, HelpScreenEffect> {
  override fun init(model: HelpScreenModel): First<HelpScreenModel, HelpScreenEffect> {
    val effects = mutableSetOf<HelpScreenEffect>()
    if (model.hasHelpContent.not()) {
      effects.add(LoadHelpContent)
    }

    return first(model, effects)
  }
}
