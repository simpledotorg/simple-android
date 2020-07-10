package org.simple.clinic.util

import android.content.Context
import org.simple.clinic.R
import org.simple.clinic.util.RelativeTimestamp.ExactDate
import org.simple.clinic.util.RelativeTimestamp.Today
import org.simple.clinic.util.RelativeTimestamp.WithinSixMonths
import org.simple.clinic.util.RelativeTimestamp.Yesterday
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import javax.inject.Inject

class RelativeTimestampGenerator @Inject constructor() {

  fun generate(instant: Instant, userClock: UserClock): RelativeTimestamp {
    val date = instant.toLocalDateAtZone(userClock.zone)
    val today = LocalDate.now(userClock)
    val yesterday = today.minusDays(1)
    val sixMonthsAgo = today.minusMonths(6)

    return when {
      date.isAfter(today) -> ExactDate(instant)
      date == today -> Today
      date == yesterday -> Yesterday
      sixMonthsOrLess(date, sixMonthsAgo) -> WithinSixMonths(DAYS.between(date, today).toInt())
      else -> ExactDate(instant)
    }
  }

  private fun sixMonthsOrLess(date: LocalDate, sixMonthsAgo: LocalDate): Boolean {
    return date == sixMonthsAgo || date.isAfter(sixMonthsAgo)
  }
}

sealed class RelativeTimestamp {

  fun displayText(context: Context, timeFormatter: DateTimeFormatter): String {
    return when (this) {
      Today -> context.getString(R.string.timestamp_today)
      Yesterday -> context.getString(R.string.timestamp_yesterday)
      is WithinSixMonths -> context.getString(R.string.timestamp_days_ago, daysBetween.toString())
      is ExactDate -> timeFormatter.format(time.atZone(ZoneOffset.UTC).toLocalDateTime())
    }
  }

  object Today : RelativeTimestamp()

  object Yesterday : RelativeTimestamp()

  data class WithinSixMonths(val daysBetween: Int) : RelativeTimestamp()

  data class ExactDate(val time: Instant) : RelativeTimestamp()
}
