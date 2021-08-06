package org.simple.clinic.patient

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.util.TestUserClock
import java.time.Instant
import java.time.LocalDate

class PatientAgeDetailsTest {

  @Test
  fun `date of birth must be guessed from the age correctly`() {
    val clock = TestUserClock()

    // given
    val `age recorded on the same day` = Age(
        value = 30,
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )
    val `age recorded a year earlier` = Age(
        value = 30,
        updatedAt = Instant.parse("2017-01-01T00:00:00Z")
    )

    // when
    val `dob from age recorded on the same day` = PatientAgeDetails(`age recorded on the same day`.value, `age recorded on the same day`.updatedAt, null)
    val `dob from age recorded a year earlier` = PatientAgeDetails(`age recorded a year earlier`.value, `age recorded a year earlier`.updatedAt, null)

    // the
    assertThat(`dob from age recorded on the same day`)
        .isEqualTo(PatientAgeDetails(
            ageValue = `dob from age recorded on the same day`.ageValue,
            ageUpdatedAt = `dob from age recorded on the same day`.ageUpdatedAt,
            dateOfBirth = null
        ))
    assertThat(`dob from age recorded a year earlier`)
        .isEqualTo(PatientAgeDetails(
            ageValue = `dob from age recorded a year earlier`.ageValue,
            ageUpdatedAt = `dob from age recorded a year earlier`.ageUpdatedAt,
            dateOfBirth = null
        ))
  }

  @Test
  fun `the current age must be estimated from the DateOfBirth correctly`() {
    // given
    val age = Age(value = 30, updatedAt = Instant.parse("2018-01-01T00:00:00Z"))

    fun estimateAgeAtDate(date: LocalDate): Int {
      val clock = TestUserClock(date)
      return PatientAgeDetails(age.value, age.updatedAt, null).estimateAge(clock)
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

    val age = Age(value = 30, updatedAt = Instant.parse("2018-01-01T00:00:00Z"))
    val dateOfBirth = PatientAgeDetails(age.value, age.updatedAt, null)

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

    val age = Age(value = 30, updatedAt = Instant.parse("2018-01-01T00:00:00Z"))
    val dateOfBirth = PatientAgeDetails(age.value, age.updatedAt, null)

    // when
    val estimatedDateOfBirth = dateOfBirth.approximateDateOfBirth(clock)

    // then
    assertThat(estimatedDateOfBirth).isEqualTo(LocalDate.parse("1988-01-01"))
  }
}
