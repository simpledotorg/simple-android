package org.simple.clinic.summary.nextappointment

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class NextAppointmentEffectHandlerTest {

  private val appointmentRepository = mock<AppointmentRepository>()
  private val uiActions = mock<NextAppointmentUiActions>()
  private val effectHandler = NextAppointmentEffectHandler(
      appointmentRepository = appointmentRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  private val patientUuid = UUID.fromString("06ffb32b-fc59-4e38-9f08-9be810b313da")

  @After
  fun teardown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load next appointment patient profile effect is received, then load the next appointment patient profile`() {
    // given
    val appointmentUuid = UUID.fromString("13dea42d-1958-412e-9db7-6f7601373245")
    val facilityUuid = UUID.fromString("095c2baa-bb05-4cce-99a8-5e21ea964117")

    val patient = TestData.patient(
        uuid = patientUuid,
        fullName = "Ramesh Mehta"
    )

    val facility = TestData.facility(
        uuid = facilityUuid,
        name = "PHC Obvious"
    )

    val appointment = TestData.appointment(
        uuid = appointmentUuid,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid
    )

    val nextAppointmentPatientProfile = NextAppointmentPatientProfile(appointment, patient, facility)

    whenever(appointmentRepository.nextAppointmentPatientProfile(patientUuid)) doReturn nextAppointmentPatientProfile

    // when
    effectHandlerTestCase.dispatch(LoadNextAppointmentPatientProfile(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(NextAppointmentPatientProfileLoaded(nextAppointmentPatientProfile))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when open schedule appointment sheet effect is received, then open the schedule appointment sheet`() {
    // when
    effectHandlerTestCase.dispatch(OpenScheduleAppointmentSheet(patientUuid))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openScheduleAppointmentSheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }
}
