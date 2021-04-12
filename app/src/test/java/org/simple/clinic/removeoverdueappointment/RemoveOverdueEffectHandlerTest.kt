package org.simple.clinic.removeoverdueappointment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class RemoveOverdueEffectHandlerTest {

  private val appointmentRepository = mock<AppointmentRepository>()
  private val effectHandler = RemoveOverdueEffectHandler(
      appointmentRepository = appointmentRepository,
      schedulersProvider = TestSchedulersProvider.trampoline()
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when mark patient as visited effect is received, then mark patient as visited`() {
    // given
    val appointmentId = UUID.fromString("e1c5ab51-da5d-4f58-bd42-d15c52e40c77")

    // when
    testCase.dispatch(MarkPatientAsVisited(appointmentId))

    // then
    verify(appointmentRepository).markAsAlreadyVisited(appointmentId)
    verifyNoMoreInteractions(appointmentRepository)

    testCase.assertOutgoingEvents(PatientMarkedAsVisited)
  }
}
