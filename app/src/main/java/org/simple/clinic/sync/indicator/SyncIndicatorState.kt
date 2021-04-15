package org.simple.clinic.sync.indicator

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Duration

sealed class SyncIndicatorState : Parcelable {
  @Parcelize object ConnectToSync : SyncIndicatorState()
  @Parcelize data class Synced(val durationSince: Duration) : SyncIndicatorState() {
    fun incrementDuration(duration: Duration): Synced {
      return copy(durationSince = durationSince.plus(duration))
    }
  }

  @Parcelize object SyncPending : SyncIndicatorState()
  @Parcelize object Syncing : SyncIndicatorState()
}
