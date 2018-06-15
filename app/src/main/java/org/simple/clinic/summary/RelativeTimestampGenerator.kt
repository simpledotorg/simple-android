package org.simple.clinic.summary

import android.content.Context
import org.simple.clinic.R
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

// TODO: Tests.
class RelativeTimestampGenerator @Inject constructor() {

  private fun generate(today: LocalDate, time: Instant): RelativeTimestamp {
    val timeDate = time.atZone(ZoneOffset.UTC).toLocalDate()

    return when {
      timeDate.dayOfMonth == today.dayOfMonth -> Today()
      timeDate.dayOfMonth == today.minusDays(1).dayOfMonth -> Yesterday()
      timeDate > timeDate.minusMonths(6) -> WithinSixMonths(Period.between(today, timeDate))
      else -> OlderThanSixMonths(time)
    }
  }

  fun generate(time: Instant): RelativeTimestamp {
    val today = LocalDate.now(ZoneOffset.UTC)
    return generate(today, time)
  }
}

sealed class RelativeTimestamp {
  abstract fun displayText(context: Context): String
}

class Today : RelativeTimestamp() {
  override fun displayText(context: Context): String {
    return context.getString(R.string.timestamp_today)
  }
}

class Yesterday : RelativeTimestamp() {
  override fun displayText(context: Context): String {
    return context.getString(R.string.timestamp_yesterday)
  }
}

class WithinSixMonths(private val period: Period) : RelativeTimestamp() {
  override fun displayText(context: Context): String {
    return context.getString(R.string.timestamp_days, period.days)
  }
}

class OlderThanSixMonths(private val time: Instant) : RelativeTimestamp() {
  override fun displayText(context: Context): String {
    return timestampFormatter.format(time)
  }

  companion object {
    val timestampFormatter = DateTimeFormatter.ofPattern("d MMM, yyyy", Locale.ENGLISH)!!
  }
}
