package org.simple.clinic.sync.indicator

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Duration

sealed class SyncIndicatorState : Parcelable{
  @Parcelize object ConnectToSync : SyncIndicatorState()
  @Parcelize data class Synced(val durationSince: Duration) : SyncIndicatorState()
  @Parcelize object SyncPending : SyncIndicatorState()
  @Parcelize object Syncing : SyncIndicatorState()
}
