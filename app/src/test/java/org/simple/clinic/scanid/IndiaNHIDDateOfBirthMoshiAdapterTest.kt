package org.simple.clinic.scanid

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import org.junit.Test
import java.time.LocalDate

class IndiaNHIDDateOfBirthMoshiAdapterTest {

  private val moshi = Moshi.Builder()
      .add(IndiaNHIDDateOfBirth::class.java, IndiaNHIDDateOfBirthMoshiAdapter())
      .build()

  @Test
  fun `india nhid date of birth round trip`() {
    val adapter = moshi.adapter(IndiaNHIDDateOfBirth::class.java)

    val value = IndiaNHIDDateOfBirth(LocalDate.of(1997, 12, 12))

    val json = adapter.toJson(value)
    val parsed = adapter.fromJson(json)

    assertThat(parsed).isEqualTo(value)
  }
}
