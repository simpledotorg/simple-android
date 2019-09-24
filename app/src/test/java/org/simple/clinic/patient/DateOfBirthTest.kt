package org.simple.clinic.patient

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.patient.DateOfBirth.Type.GUESSED
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
            type = GUESSED
        ))
    assertThat(`dob from age recorded a year earlier`)
        .isEqualTo(DateOfBirth(
            date = LocalDate.parse("1987-01-01"),
            type = GUESSED
        ))
  }
}
