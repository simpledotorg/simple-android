package org.simple.clinic.patient

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.sharedTestCode.util.TestUserClock
import java.time.Instant
import java.time.LocalDate

class PatientAgeDetailsTest {

  @Test
  fun `the current age must be estimated from the DateOfBirth correctly`() {
    // given
    fun estimateAgeAtDate(date: LocalDate): Int {
      val clock = TestUserClock(date)
      return PatientAgeDetails(
          ageValue = 30,
          ageUpdatedAt = Instant.parse("2018-01-01T00:00:00Z"),
          dateOfBirth = null
      ).estimateAge(clock)
    }

    // then
    val `estimated age on the same day` = estimateAgeAtDate(LocalDate.parse("2018-01-01"))
    assertThat(`estimated age on the same day`).isEqualTo(30)

    val `estimated age almost a year later` = estimateAgeAtDate(LocalDate.parse("2018-12-31"))
    assertThat(`estimated age almost a year later`).isEqualTo(30)

    val `estimated age a year later` = estimateAgeAtDate(LocalDate.parse("2019-01-01"))
    assertThat(`estimated age a year later`).isEqualTo(31)

    val `estimated age a year and six months later` = estimateAgeAtDate(LocalDate.parse("2019-07-01"))
    assertThat(`estimated age a year and six months later`).isEqualTo(31)

    val `estimated age two years later` = estimateAgeAtDate(LocalDate.parse("2020-01-01"))
    assertThat(`estimated age two years later`).isEqualTo(32)

    val `estimated age two years and nine months later` = estimateAgeAtDate(LocalDate.parse("2020-10-01"))
    assertThat(`estimated age two years and nine months later`).isEqualTo(32)

    val `estimated age three years and a day later` = estimateAgeAtDate(LocalDate.parse("2021-01-02"))
    assertThat(`estimated age three years and a day later`).isEqualTo(33)
  }

  @Test
  fun `the current age must be estimated from the recorded age correctly`() {
    // given
    val currentTime = Instant.parse("2020-01-01T00:00:00Z")
    val clock = TestUserClock(currentTime)

    val dateOfBirth = PatientAgeDetails(
        ageValue = 30,
        ageUpdatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        dateOfBirth = null
    )

    // when
    val estimatedAge = dateOfBirth.estimateAge(clock)

    // then
    assertThat(estimatedAge).isEqualTo(32)
  }

  @Test
  fun `the current age must be estimated from the recorded date of birth correctly`() {
    // given
    val currentTime = Instant.parse("2020-01-01T00:00:00Z")
    val clock = TestUserClock(currentTime)

    val dateOfBirth = PatientAgeDetails(
        ageValue = null,
        ageUpdatedAt = null,
        dateOfBirth = LocalDate.parse("1988-01-01")
    )

    // when
    val estimatedAge = dateOfBirth.estimateAge(clock)

    // then
    assertThat(estimatedAge).isEqualTo(32)
  }

  @Test
  fun `the date of birth must be estimated from the recorded age correctly`() {
    // given
    val currentTime = Instant.parse("2020-01-01T00:00:00Z")
    val clock = TestUserClock(currentTime)

    val dateOfBirth = PatientAgeDetails(
        ageValue = 30,
        ageUpdatedAt = Instant.parse("2018-01-01T00:00:00Z"),
        dateOfBirth = null
    )

    // when
    val estimatedDateOfBirth = dateOfBirth.approximateDateOfBirth(clock)

    // then
    assertThat(estimatedDateOfBirth).isEqualTo(LocalDate.parse("1988-01-01"))
  }
}
