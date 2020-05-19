package org.simple.clinic.sync.indicator

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class SyncIndicatorInit : Init<SyncIndicatorModel, SyncIndicatorEffect> {
  override fun init(model: SyncIndicatorModel): First<SyncIndicatorModel, SyncIndicatorEffect> {
    return first(model)
  }
}
