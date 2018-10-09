package org.simple.clinic.summary

import android.content.Context
import org.simple.clinic.R
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

class RelativeTimestampGenerator @Inject constructor() {

  fun generate(today: LocalDateTime, time: Instant): RelativeTimestamp {
    val dateTime = time.atZone(ZoneOffset.UTC).toLocalDateTime()

    val todayAtMidnight = today.truncatedTo(ChronoUnit.DAYS)
    val yesterdayAtMidnight = todayAtMidnight.minusDays(1)

    return when {
      dateTime > todayAtMidnight -> Today
      dateTime > yesterdayAtMidnight -> Yesterday
      dateTime > today.minusMonths(6) -> WithinSixMonths(ChronoUnit.DAYS.between(dateTime, today))
      else -> OlderThanSixMonths(time)
    }
  }

  fun generate(time: Instant): RelativeTimestamp {
    val today = LocalDateTime.now(ZoneOffset.UTC)
    return generate(today, time)
  }
}

sealed class RelativeTimestamp {
  abstract fun displayText(context: Context): String
}

object Today : RelativeTimestamp() {
  override fun displayText(context: Context): String {
    return context.getString(R.string.timestamp_today)
  }
}

object Yesterday : RelativeTimestamp() {
  override fun displayText(context: Context): String {
    return context.getString(R.string.timestamp_yesterday)
  }
}

data class WithinSixMonths(private val daysBetween: Long) : RelativeTimestamp() {
  override fun displayText(context: Context): String {
    return context.getString(R.string.timestamp_days, daysBetween)
  }
}

class OlderThanSixMonths(private val time: Instant) : RelativeTimestamp() {
  override fun displayText(context: Context): String {
    return timestampFormatter.format(time.atZone(ZoneOffset.UTC).toLocalDateTime())
  }

  companion object {
    val timestampFormatter = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH)!!
  }
}
