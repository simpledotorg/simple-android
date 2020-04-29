package org.simple.clinic.patient

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.patient.DateOfBirth.Type.FROM_AGE
import org.simple.clinic.util.TestUserClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

class DateOfBirthTest {

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
    val `dob from age recorded on the same day` = DateOfBirth.fromAge(`age recorded on the same day`, clock)
    val `dob from age recorded a year earlier` = DateOfBirth.fromAge(`age recorded a year earlier`, clock)

    // the
    assertThat(`dob from age recorded on the same day`)
        .isEqualTo(DateOfBirth(
            date = LocalDate.parse("1988-01-01"),
            type = FROM_AGE
        ))
    assertThat(`dob from age recorded a year earlier`)
        .isEqualTo(DateOfBirth(
            date = LocalDate.parse("1987-01-01"),
            type = FROM_AGE
        ))
  }

  @Test
  fun `the current age must be estimated from the DateOfBirth correctly`() {
    // given
    val age = Age(value = 30, updatedAt = Instant.parse("2018-01-01T00:00:00Z"))

    fun estimateAgeAtDate(date: LocalDate): Int {
      val clock = TestUserClock(date)
      return DateOfBirth.fromAge(age, clock).estimateAge(clock)
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
}
