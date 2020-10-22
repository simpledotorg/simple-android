package org.simple.clinic.scheduleappointment

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.newentry.ButtonState
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.util.TestUserClock
import java.time.LocalDate
import java.time.Period
import java.util.UUID

class ScheduleAppointmentInitTest {

  private val initSpec = InitSpec(ScheduleAppointmentInit())
  private val patientUuid = UUID.fromString("a6e8ea46-a18e-47d2-b3e9-3a1d1c8ebf4f")
  private val clock = TestUserClock(LocalDate.parse("2020-10-26"))

  @Test
  fun `when the screen is created, load teleconsult record details`() {

    val appointmentConfig: AppointmentConfig = AppointmentConfig(
        appointmentDuePeriodForDefaulters = Period.ofDays(30),
        scheduleAppointmentsIn = listOf(TimeToAppointment.Days(3)),
        defaultTimeToAppointment = TimeToAppointment.Days(5),
        periodForIncludingOverdueAppointments = Period.ofMonths(12),
        remindAppointmentsIn = emptyList())

    val model = ScheduleAppointmentModel.create(
        patientUuid = patientUuid,
        timeToAppointments = appointmentConfig.scheduleAppointmentsIn,
        userClock = clock,
        doneButtonState = ButtonState.SAVED
    )

    initSpec
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(LoadTeleconsultRecord(patientUuid))
            )
        )
  }
}
