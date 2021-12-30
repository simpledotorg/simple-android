package org.simple.clinic.summary.nextappointment

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class NextAppointmentEffectHandlerTest {

  private val appointmentRepository = mock<AppointmentRepository>()
  private val effectHandler = NextAppointmentEffectHandler(
      appointmentRepository = appointmentRepository,
      schedulersProvider = TestSchedulersProvider.trampoline()
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun teardown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load appointment effect is received, then load the appointment`() {
    // given
    val appointmentUuid = UUID.fromString("13dea42d-1958-412e-9db7-6f7601373245")
    val patientUuid = UUID.fromString("06ffb32b-fc59-4e38-9f08-9be810b313da")
    val appointment = TestData.appointment(
        uuid = appointmentUuid,
        patientUuid = patientUuid
    )

    whenever(appointmentRepository.latestAppointmentForPatient(patientUuid)) doReturn Observable.just(appointment)

    // when
    effectHandlerTestCase.dispatch(LoadAppointment(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(AppointmentLoaded(appointment))
  }
}
