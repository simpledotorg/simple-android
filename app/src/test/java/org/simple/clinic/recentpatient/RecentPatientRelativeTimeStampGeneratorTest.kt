package org.simple.clinic.recentpatient

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.recentpatient.RelativeTimestamp.OlderThanTwoDays
import org.simple.clinic.recentpatient.RelativeTimestamp.Today
import org.simple.clinic.recentpatient.RelativeTimestamp.Yesterday
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset

class RecentPatientRelativeTimeStampGeneratorTest {

  @Test
  fun `generated timestamps should be correct`() {
    val zone = ZoneOffset.UTC
    val generator = RecentPatientRelativeTimeStampGenerator(zone)

    val today = LocalDate.now(zone)
    assertThat(generator.generate(today, today)).isEqualTo(Today)

    val yesterday = today.minusDays(1)
    assertThat(generator.generate(today, yesterday)).isEqualTo(Yesterday)

    val twoDaysOld = today.minusDays(2)
    assertThat(generator.generate(today, twoDaysOld)).isEqualTo(OlderThanTwoDays(twoDaysOld))
  }
}
