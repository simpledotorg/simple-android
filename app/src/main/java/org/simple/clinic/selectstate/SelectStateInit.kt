package org.simple.clinic.selectstate

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class SelectStateInit : Init<SelectStateModel, SelectStateEffect> {

  override fun init(model: SelectStateModel): First<SelectStateModel, SelectStateEffect> {
    val effects = mutableSetOf<SelectStateEffect>()
    if (!model.hasStates) {
      effects.add(LoadStates)
    }

    return first(model, effects)
  }
}
