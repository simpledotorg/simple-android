package org.simple.clinic.sync.indicator

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class SyncIndicatorInit : Init<SyncIndicatorModel, SyncIndicatorEffect> {
  override fun init(model: SyncIndicatorModel): First<SyncIndicatorModel, SyncIndicatorEffect> {
    return first(model, FetchLastSyncedStatus, FetchPendingSyncRecordsState)
  }
}
