package org.simple.clinic.appupdate.criticalupdatedialog

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class CriticalAppUpdateInit : Init<CriticalAppUpdateModel, CriticalAppUpdateEffect> {

  override fun init(model: CriticalAppUpdateModel): First<CriticalAppUpdateModel, CriticalAppUpdateEffect> {
    val effects = mutableSetOf<CriticalAppUpdateEffect>()
    if (!model.hasHelpContact) {
      effects.add(LoadAppUpdateHelpContact)
    }

    return first(model, effects)
  }
}
