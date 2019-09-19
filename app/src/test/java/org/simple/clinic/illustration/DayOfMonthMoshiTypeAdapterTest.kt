package org.simple.clinic.illustration

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.threeten.bp.Month

class DayOfMonthMoshiTypeAdapterTest {

  private val typeAdapter = DayOfMonth.MoshiTypeAdapter

  @Test
  fun `DayOfMonth must get serialized correctly`() {
    val serialized = typeAdapter.fromModel(DayOfMonth(6, Month.JANUARY))
    assertThat(serialized).isEqualTo("6 JANUARY")
  }

  @Test
  fun `DayOfMonth must get deserialized correctly`() {
    val deserialized = typeAdapter.toModel("6 January")
    assertThat(deserialized).isEqualTo(DayOfMonth(6, Month.JANUARY))
  }
}
