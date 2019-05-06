package org.simple.clinic.recentpatient

import androidx.annotation.VisibleForTesting
import org.simple.clinic.recentpatient.RelativeTimestamp.OlderThanTwoDays
import org.simple.clinic.recentpatient.RelativeTimestamp.Today
import org.simple.clinic.recentpatient.RelativeTimestamp.Yesterday
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import javax.inject.Inject

class RecentPatientRelativeTimeStampGenerator @Inject constructor(
    private val systemDefaultZone: ZoneId
) {

  @VisibleForTesting
  fun generate(today: LocalDate, date: LocalDate): RelativeTimestamp =
      when (date) {
        today -> Today
        today.minusDays(1) -> Yesterday
        else -> OlderThanTwoDays(date)
      }

  fun generate(date: LocalDate): RelativeTimestamp {
    val today = LocalDate.now(systemDefaultZone)
    return generate(today, date)
  }
}

sealed class RelativeTimestamp {

  object Today : RelativeTimestamp()

  object Yesterday : RelativeTimestamp()

  data class OlderThanTwoDays(val date: LocalDate) : RelativeTimestamp()
}
