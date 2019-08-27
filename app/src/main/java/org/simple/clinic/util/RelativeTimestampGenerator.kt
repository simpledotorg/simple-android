package org.simple.clinic.util

import android.content.Context
import org.simple.clinic.R
import org.simple.clinic.util.RelativeTimestamp.ExactDate
import org.simple.clinic.util.RelativeTimestamp.Today
import org.simple.clinic.util.RelativeTimestamp.WithinSixMonths
import org.simple.clinic.util.RelativeTimestamp.Yesterday
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit.DAYS
import javax.inject.Inject

class RelativeTimestampGenerator @Inject constructor() {

  fun generate(instant: Instant, userClock: UserClock): RelativeTimestamp {
    val then = instant.toLocalDateAtZone(userClock.zone)
    val today = LocalDate.now(userClock)
    val yesterday = today.minusDays(1)
    val sixMonthsAgo = today.minusMonths(6)

    return when {
      then.isAfter(today) -> ExactDate(instant)
      then == today -> Today
      then == yesterday -> Yesterday
      sixMonthsOrLess(then, sixMonthsAgo) -> WithinSixMonths(DAYS.between(then, today).toInt())
      else -> ExactDate(instant)
    }
  }

  private fun sixMonthsOrLess(then: LocalDate, sixMonthsAgo: LocalDate): Boolean {
    return then == sixMonthsAgo || then.isAfter(sixMonthsAgo)
  }
}

sealed class RelativeTimestamp {

  fun displayText(context: Context, timeFormatter: DateTimeFormatter): String {
    return when (this) {
      Today -> context.getString(R.string.timestamp_today)
      Yesterday -> context.getString(R.string.timestamp_yesterday)
      is WithinSixMonths -> context.getString(R.string.timestamp_days, daysBetween)
      is ExactDate -> timeFormatter.format(time.atZone(ZoneOffset.UTC).toLocalDateTime())
    }
  }

  object Today : RelativeTimestamp()

  object Yesterday : RelativeTimestamp()

  data class WithinSixMonths(val daysBetween: Int) : RelativeTimestamp()

  data class ExactDate(val time: Instant) : RelativeTimestamp()
}
