package org.simple.clinic.sync

import android.os.Parcelable
import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.parcelize.Parcelize
import org.simple.clinic.sync.SyncProgress.FAILURE
import org.simple.clinic.sync.SyncProgress.SUCCESS
import org.simple.clinic.sync.SyncProgress.SYNCING
import org.simple.clinic.util.UtcClock
import java.time.Instant

@JsonClass(generateAdapter = true)
@Parcelize
data class LastSyncedState(
    val lastSyncProgress: SyncProgress? = null,
    val lastSyncSucceededAt: Instant? = null
) : Parcelable {

  fun syncStarted(): LastSyncedState {
    return this.copy(lastSyncProgress = SYNCING)
  }

  fun syncFailed(): LastSyncedState {
    return this.copy(lastSyncProgress = FAILURE)
  }

  fun syncedSuccessfully(clock: UtcClock): LastSyncedState {
    return this.copy(lastSyncProgress = SUCCESS, lastSyncSucceededAt = Instant.now(clock))
  }

  fun isEmpty(): Boolean {
    return lastSyncProgress == null
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
