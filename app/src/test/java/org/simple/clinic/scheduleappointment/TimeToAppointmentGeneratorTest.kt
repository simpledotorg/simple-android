package org.simple.clinic.scheduleappointment

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.scheduleappointment.TimeToAppointment.Days
import org.simple.clinic.scheduleappointment.TimeToAppointment.Months
import org.simple.clinic.scheduleappointment.TimeToAppointment.Weeks
import org.threeten.bp.LocalDate

@RunWith(JUnitParamsRunner::class)
class TimeToAppointmentGeneratorTest {

  @Test
  @Parameters(method = "params for calculating time to appointment")
  fun `time to appointment from a given date must be calculated`(params: ParamsForCalculatingTimeToAppointment) {
    // given
    val (currentDate: LocalDate,
        appointmentDate: LocalDate,
        expectedTimeToAppointment: TimeToAppointment) = params

    // when
    val timeToAppointment = TimeToAppointment.from(currentDate = currentDate, appointmentDate = appointmentDate)

    // then
    assertThat(timeToAppointment).isEqualTo(expectedTimeToAppointment)
  }

  @Suppress("Unused")
  private fun `params for calculating time to appointment`(): List<ParamsForCalculatingTimeToAppointment> {
    return listOf(
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-02",
            expectedTimeToAppointment = Days(1)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-04",
            expectedTimeToAppointment = Days(3)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-29",
            expectedTimeToAppointment = Days(28)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-08",
            expectedTimeToAppointment = Days(7)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-09",
            expectedTimeToAppointment = Days(8)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-14",
            expectedTimeToAppointment = Days(13)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-15",
            expectedTimeToAppointment = Weeks(2)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-16",
            expectedTimeToAppointment = Days(15)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-21",
            expectedTimeToAppointment = Days(20)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-22",
            expectedTimeToAppointment = Weeks(3)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-01-23",
            expectedTimeToAppointment = Days(22)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-02-01",
            expectedTimeToAppointment = Months(1)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-02-05",
            expectedTimeToAppointment = Days(35)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-02-28",
            expectedTimeToAppointment = Days(58)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-03-01",
            expectedTimeToAppointment = Months(2)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-03-20",
            expectedTimeToAppointment = Days(78)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-04-01",
            expectedTimeToAppointment = Months(3)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-05-01",
            expectedTimeToAppointment = Months(4)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-06-01",
            expectedTimeToAppointment = Months(5)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-07-01",
            expectedTimeToAppointment = Months(6)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-08-01",
            expectedTimeToAppointment = Months(7)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-09-01",
            expectedTimeToAppointment = Months(8)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-10-01",
            expectedTimeToAppointment = Months(9)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-11-01",
            expectedTimeToAppointment = Months(10)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-12-01",
            expectedTimeToAppointment = Months(11)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2019-12-31",
            expectedTimeToAppointment = Days(364)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2020-01-01",
            expectedTimeToAppointment = Months(12)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2020-01-02",
            expectedTimeToAppointment = Days(366)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2020-02-01",
            expectedTimeToAppointment = Days(396)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-05",
            appointmentDate = "2019-01-12",
            expectedTimeToAppointment = Days(7)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-05",
            appointmentDate = "2019-02-04",
            expectedTimeToAppointment = Days(30)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-05",
            appointmentDate = "2019-02-05",
            expectedTimeToAppointment = Months(1)
        ),
        ParamsForCalculatingTimeToAppointment(
            currentDate = "2019-01-01",
            appointmentDate = "2021-01-01",
            expectedTimeToAppointment = Days(731)
        )
    )
  }

  data class ParamsForCalculatingTimeToAppointment(
      val currentDate: LocalDate,
      val appointmentDate: LocalDate,
      val expectedTimeToAppointment: TimeToAppointment
  ) {
    constructor(
        currentDate: String,
        appointmentDate: String,
        expectedTimeToAppointment: TimeToAppointment
    ) : this(
        currentDate = LocalDate.parse(currentDate),
        appointmentDate = LocalDate.parse(appointmentDate),
        expectedTimeToAppointment = expectedTimeToAppointment
    )
  }
}
