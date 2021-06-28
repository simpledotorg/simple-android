package org.simple.clinic.patient.onlinelookup.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Duration
import java.time.Instant

class RecordRetentionTest {

  @Test
  fun `when computing the time to retain a temporary record, the retention period must be added to the current time`() {
    // given
    val retentionDuration = Duration.ofDays(1)
    val retention = RecordRetention(
        type = RetentionType.Temporary,
        retainFor = retentionDuration
    )
    val currentTime = Instant.parse("2018-01-01T09:33:05Z")

    // when
    val retainUntil = retention.computeRetainUntilTimestamp(currentTime)

    // then
    assertThat(retainUntil).isNotNull()
    assertThat(retainUntil).isEqualTo(Instant.parse("2018-01-02T09:33:05Z"))
  }

  @Test
  fun `when computing the time to retain a permanent record, the retention period must be ignored`() {
    // given
    val retention = RecordRetention(
        type = RetentionType.Permanent,
        retainFor = null
    )
    val currentTime = Instant.parse("2018-01-01T00:00:00Z")

    // when
    val retainUntil = retention.computeRetainUntilTimestamp(currentTime)

    // then
    assertThat(retainUntil).isNull()
  }
}
