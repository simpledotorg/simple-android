package org.simple.clinic.util

import android.content.Context
import androidx.annotation.VisibleForTesting
import org.simple.clinic.R
import org.simple.clinic.util.RelativeTimestamp.ExactDate
import org.simple.clinic.util.RelativeTimestamp.Today
import org.simple.clinic.util.RelativeTimestamp.WithinSixMonths
import org.simple.clinic.util.RelativeTimestamp.Yesterday
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit.DAYS
import javax.inject.Inject

class RelativeTimestampGenerator @Inject constructor() {

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  fun generate(today: LocalDateTime, time: Instant): RelativeTimestamp {
    val dateTime = time.atZone(ZoneOffset.UTC).toLocalDateTime()

    val todayAtMidnight = today.truncatedTo(DAYS)
    val yesterdayAtMidnight = todayAtMidnight.minusDays(1)
    val tomorrowAtMidnight = todayAtMidnight.plusDays(1)

    return when {
      dateTime > tomorrowAtMidnight -> ExactDate(time)
      dateTime > todayAtMidnight -> Today
      dateTime > yesterdayAtMidnight -> Yesterday
      dateTime > today.minusMonths(6) -> WithinSixMonths(daysBetween(dateTime, today))
      else -> ExactDate(time)
    }
  }

  private fun daysBetween(dateTime: LocalDateTime, today: LocalDateTime) =
      DAYS.between(dateTime.toLocalDate(), today.toLocalDate())

  fun generate(time: Instant): RelativeTimestamp {
    val today = LocalDateTime.now(ZoneOffset.UTC)
    return generate(today, time)
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

  data class WithinSixMonths(val daysBetween: Long) : RelativeTimestamp()

  data class ExactDate(val time: Instant) : RelativeTimestamp()
}
