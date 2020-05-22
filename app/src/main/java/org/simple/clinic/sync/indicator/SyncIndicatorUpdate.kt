package org.simple.clinic.sync.indicator

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class SyncIndicatorUpdate : Update<SyncIndicatorModel, SyncIndicatorEvent, SyncIndicatorEffect> {
  override fun update(model: SyncIndicatorModel, event: SyncIndicatorEvent):
      Next<SyncIndicatorModel, SyncIndicatorEffect> {

    return when (event) {
      is LastSyncedStateFetched -> next(model.lastSyncedStateChanged(event.lastSyncState))
      is DataForSyncIndicatorStateFetched -> noChange()
      is IncrementTimerTick -> noChange()
    }
  }
}
