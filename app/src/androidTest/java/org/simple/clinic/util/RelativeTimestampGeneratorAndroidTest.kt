package org.simple.clinic.util

import android.app.Application
import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class RelativeTimestampGeneratorAndroidTest {

  @Inject
  lateinit var context: Application

  private val generator = RelativeTimestampGenerator()

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun it_should_generate_timestamp_strings() {
    val todayDateTime = LocalDateTime.of(2018, 7, 29, 16, 58)

    val todayTime = LocalDateTime.of(2018, 7, 29, 2, 0, 0).atOffset(ZoneOffset.UTC).toInstant()
    assertThat(generator.generate(todayDateTime, todayTime).displayText(context)).isNotEmpty()

    val yesterdayButWithin24Hours = LocalDateTime.of(2018, 7, 28, 23, 0, 0).atOffset(ZoneOffset.UTC).toInstant()
    assertThat(generator.generate(todayDateTime, yesterdayButWithin24Hours).displayText(context)).isNotEmpty()

    val yesterdayButOutside24Hours = LocalDateTime.of(2018, 7, 28, 3, 0, 0).atOffset(ZoneOffset.UTC).toInstant()
    assertThat(generator.generate(todayDateTime, yesterdayButOutside24Hours).displayText(context)).isNotEmpty()

    val withinSixMonths = LocalDateTime.of(2018, 3, 29, 4, 0, 0).atOffset(ZoneOffset.UTC).toInstant()
    assertThat(generator.generate(todayDateTime, withinSixMonths).displayText(context)).isNotEmpty()

    val olderThanSixMonths = LocalDateTime.of(2017, 1, 27, 3, 0, 0).atOffset(ZoneOffset.UTC).toInstant()
    assertThat(generator.generate(todayDateTime, olderThanSixMonths).displayText(context)).isNotEmpty()
  }
}
