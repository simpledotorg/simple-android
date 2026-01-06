package org.simple.clinic.patient.onlinelookup

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import org.junit.Test
import org.simple.clinic.patient.onlinelookup.api.DurationFromSecondsMoshiAdapter
import org.simple.clinic.patient.onlinelookup.api.RecordRetention
import org.simple.clinic.patient.onlinelookup.api.RetentionType
import org.simple.clinic.patient.onlinelookup.api.SecondsDuration
import java.time.Duration

class RetentionMoshiAdapterTest {

  private val moshi = Moshi
      .Builder()
      .add(SecondsDuration::class.java, DurationFromSecondsMoshiAdapter())
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
        retainFor = SecondsDuration(Duration.ofSeconds(10000))
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

  @Test
  fun `seconds duration round trip`() {
    val adapter = moshi.adapter(SecondsDuration::class.java)

    val value = SecondsDuration(Duration.ofSeconds(123))

    val json = adapter.toJson(value)
    val parsed = adapter.fromJson(json)

    assertThat(parsed).isEqualTo(value)
  }

}
