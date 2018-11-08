package org.simple.clinic.security.pin

import com.f2prateek.rx.preferences2.Preference
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class BruteForceProtectionState(
    val failedAuthCount: Int = 0,
    val limitReachedAt: Optional<Instant> = None
) {

  class RxPreferencesConverter(moshi: Moshi) : Preference.Converter<BruteForceProtectionState> {

    private val adapter by lazy { moshi.adapter(BruteForceProtectionState::class.java) }

    override fun deserialize(serialized: String): BruteForceProtectionState {
      return adapter.fromJson(serialized)!!
    }

    override fun serialize(value: BruteForceProtectionState): String {
      return adapter.toJson(value)
    }
  }
}
