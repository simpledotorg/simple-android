package org.simple.clinic.util

import android.app.Application
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.simple.clinic.util.RelativeTimestamp.ExactDate
import org.simple.clinic.util.RelativeTimestamp.Today
import org.simple.clinic.util.RelativeTimestamp.WithinSixMonths
import org.simple.clinic.util.RelativeTimestamp.Yesterday
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

@RunWith(AndroidJUnit4::class)
class RelativeTimestampGeneratorAndroidTest {

  @Inject
  lateinit var context: Application

  @field:[Inject Named("exact_date")]
  lateinit var formatter: DateTimeFormatter

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun it_should_generate_timestamp_strings() {
    // given
    val today = Today
    val yesterday = Yesterday
    val withinSixMonths = WithinSixMonths(100)
    val exactDate = ExactDate(Instant.EPOCH)

    // when
    val todayDisplayText = today.displayText(context, formatter)
    val yesterdayDisplayText = yesterday.displayText(context, formatter)
    val withinSixMonthsDisplayText = withinSixMonths.displayText(context, formatter)
    val exactDateDisplayText = exactDate.displayText(context, formatter)

    // then
    assertThat(todayDisplayText).isNotEmpty()
    assertThat(yesterdayDisplayText).isNotEmpty()
    assertThat(withinSixMonthsDisplayText).isNotEmpty()
    assertThat(exactDateDisplayText).isNotEmpty()
  }
}
