package org.simple.clinic.sync.indicator

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class SyncIndicatorUpdate : Update<SyncIndicatorModel, SyncIndicatorEvent, SyncIndicatorEffect> {
  override fun update(model: SyncIndicatorModel, event: SyncIndicatorEvent):
      Next<SyncIndicatorModel, SyncIndicatorEffect> = noChange()
}
