package org.simple.clinic.security.pin

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.di.network.NetworkModule
import org.simple.clinic.util.Just
import java.time.Instant

class BruteForceProtectionStateTest {

  /**
   * If you're seeing this test fail, it means that you modified
   * [BruteForceProtectionState] without thinking about migration.
   *
   * The SharedPreferences key for [BruteForceProtectionState] is
   * versioned, so the migration can potentially happen when it's
   * being read from SharedPreferences.
   */
  @Test
  fun `fail when a migration is required`() {
    val moshi = NetworkModule().moshi()
    val adapter = moshi.adapter(BruteForceProtectionState::class.java)

    val expectedJson = """
      {
        "failedAuthCount": 5,
        "limitReachedAt": "1970-01-01T00:00:00Z"
      }
    """
    val deserialized = adapter.fromJson(expectedJson)
    assertThat(deserialized).isEqualTo(BruteForceProtectionState(failedAuthCount = 5, limitReachedAt = Just(Instant.EPOCH)))
  }
}
