package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class LastSyncedState(
    val lastSyncProgress: SyncProgress? = null,
    val lastSyncSucceededAt: Instant? = null
) {

  fun syncStarted(): LastSyncedState {
    return this.copy(lastSyncProgress = SYNCING)
  }

  fun syncFailed(): LastSyncedState {
    return this.copy(lastSyncProgress = FAILURE)
  }

  fun syncedSuccessfully(clock: UtcClock): LastSyncedState {
    return this.copy(lastSyncProgress = SUCCESS, lastSyncSucceededAt = Instant.now(clock))
  }

  class RxPreferenceConverter(moshi: Moshi) : Preference.Converter<LastSyncedState> {

    private val adapter by lazy { moshi.adapter(LastSyncedState::class.java) }

    override fun deserialize(serialized: String): LastSyncedState {
      return adapter.fromJson(serialized)!!
    }

    override fun serialize(value: LastSyncedState): String {
      return adapter.toJson(value)
    }
  }

}
