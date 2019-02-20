package org.simple.clinic.sync

import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class LastSyncedState(
    val lastSyncProgress: SyncProgress? = null,
    val lastSyncSuccessTimestamp: Instant? = null
) {

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
