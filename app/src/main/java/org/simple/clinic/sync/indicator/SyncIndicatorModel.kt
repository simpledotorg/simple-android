package org.simple.clinic.sync.indicator

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.sync.LastSyncedState

@Parcelize
data class SyncIndicatorModel(
    val lastSyncedState: LastSyncedState?,
    val syncIndicatorState: SyncIndicatorState?
) : Parcelable {

  companion object {
    fun create(): SyncIndicatorModel {
      return SyncIndicatorModel(null, null)
    }
  }

  fun lastSyncedStateChanged(newState: LastSyncedState): SyncIndicatorModel {
    return copy(lastSyncedState = newState)
  }

  fun syncIndicatorStateChanged(newState: SyncIndicatorState): SyncIndicatorModel {
    return copy(syncIndicatorState = newState)
  }
}
