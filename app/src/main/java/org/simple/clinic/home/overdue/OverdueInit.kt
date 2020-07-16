package org.simple.clinic.home.overdue

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class OverdueInit: Init<OverdueModel, OverdueEffect> {

  override fun init(model: OverdueModel): First<OverdueModel, OverdueEffect> {
    val effects = mutableSetOf<OverdueEffect>()
    
    if(!model.hasLoadedCurrentFacility) {
      effects.add(LoadCurrentFacility)
    }

    return first(model, effects)
  }
}
