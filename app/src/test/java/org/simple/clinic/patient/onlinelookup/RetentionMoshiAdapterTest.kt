package org.simple.clinic.patient.onlinelookup

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import org.junit.Test
import org.simple.clinic.patient.onlinelookup.api.DurationFromSecondsMoshiAdapter
import org.simple.clinic.patient.onlinelookup.api.RecordRetention
import org.simple.clinic.patient.onlinelookup.api.RetentionType
import java.time.Duration

class RetentionMoshiAdapterTest {

  private val moshi = Moshi
      .Builder()
      .add(DurationFromSecondsMoshiAdapter())
      .build()

  private val adapter = moshi.adapter(RecordRetention::class.java)

  @Test
  fun `parsing temporary retention period should read the type and duration`() {
    // given
    val json = """
      {
        "type": "temporary",
        "duration_seconds": 10000
      }
    """.trimIndent()

    // when
    val retention = adapter.fromJson(json)!!

    // then
    val expected = RecordRetention(
        type = RetentionType.Temporary,
        retainFor = Duration.ofSeconds(10000)
    )
    assertThat(retention).isEqualTo(expected)
  }

  @Test
  fun `parsing permanent retention period should read the type and duration`() {
    // given
    val json = """
      {
        "type": "permanent"
      }
    """.trimIndent()

    // when
    val retention = adapter.fromJson(json)!!

    // then
    val expected = RecordRetention(
        type = RetentionType.Permanent,
        retainFor = null
    )
    assertThat(retention).isEqualTo(expected)
  }
}
