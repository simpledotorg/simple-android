package org.simple.clinic.sync.indicator

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Duration

sealed class SyncIndicatorState : Parcelable {
  @Parcelize data object ConnectToSync : SyncIndicatorState()
  @Parcelize data class Synced(val durationSince: Duration) : SyncIndicatorState() {
    fun incrementDuration(duration: Duration): Synced {
      return copy(durationSince = durationSince.plus(duration))
    }
  }

  @Parcelize data object SyncPending : SyncIndicatorState()
  @Parcelize data object Syncing : SyncIndicatorState()
}
