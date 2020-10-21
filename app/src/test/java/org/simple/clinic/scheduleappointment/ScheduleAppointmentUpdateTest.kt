package org.simple.clinic.scheduleappointment

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.newentry.ButtonState.SAVED
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.util.TestUserClock
import java.time.LocalDate
import java.time.Period
import java.util.UUID

class ScheduleAppointmentUpdateTest {

  private val clock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val appointmentConfig: AppointmentConfig = AppointmentConfig(
      appointmentDuePeriodForDefaulters = Period.ofDays(30),
      scheduleAppointmentsIn = listOf(TimeToAppointment.Days(1)),
      defaultTimeToAppointment = TimeToAppointment.Days(1),
      periodForIncludingOverdueAppointments = Period.ofMonths(12),
      remindAppointmentsIn = emptyList()
  )
  private val updateSpec = UpdateSpec(ScheduleAppointmentUpdate(
      currentDate = LocalDate.now(clock),
      defaulterAppointmentPeriod = appointmentConfig.appointmentDuePeriodForDefaulters
  ))
  private val patientUuid = UUID.fromString("4fc3e3b9-2d91-4cee-9ab8-8b273320f57c")
  private val model = ScheduleAppointmentModel.create(
      patientUuid = patientUuid,
      timeToAppointments = appointmentConfig.scheduleAppointmentsIn,
      userClock = clock,
      doneButtonState = SAVED
  )

  @Test
  fun `when appointment facilities are loaded, then set current facility for schedule appointment if assigned facility is not present`() {
    val currentFacility = TestData.facility(uuid = UUID.fromString("ca1a3f3a-85e8-4132-b6f5-155b75366a3b"))

    updateSpec
        .given(model)
        .whenEvent(AppointmentFacilitiesLoaded(null, currentFacility))
        .then(assertThatNext(
            hasModel(model.appointmentFacilitySelected(currentFacility)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when appointment facilities are loaded, then set assigned facility for schedule appointment if assigned facility is present`() {
    val assignedFacility = TestData.facility(uuid = UUID.fromString("a6642a7e-3f9a-41c1-a0e1-1f651014ea48"))
    val currentFacility = TestData.facility(uuid = UUID.fromString("ca1a3f3a-85e8-4132-b6f5-155b75366a3b"))

    updateSpec
        .given(model)
        .whenEvent(AppointmentFacilitiesLoaded(assignedFacility, currentFacility))
        .then(assertThatNext(
            hasModel(model.appointmentFacilitySelected(assignedFacility)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when teleconsult record is loaded, then update the model`() {
    val teleconsultRecord = TestData.teleconsultRecord(
        id = UUID.fromString("f60e5a36-824d-48c6-a5eb-01a2184c8b97"),
        teleconsultRequestInfo = TestData.teleconsultRequestInfo()
    )

    updateSpec
        .given(model)
        .whenEvent(TeleconsultRecordLoaded(teleconsultRecord))
        .then(
            assertThatNext(
                hasModel(model.teleconsultRecordLoaded(teleconsultRecord)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when teleconsult record is not loaded, then do nothing`(){
    updateSpec
        .given(model)
        .whenEvent(TeleconsultRecordLoaded(null))
        .then(
            assertThatNext(
                hasModel(model.teleconsultRecordLoaded(null)),
                hasNoEffects()
            )
        )
  }
}
