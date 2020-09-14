package org.simple.clinic.teleconsultlog.success

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.GoToHomeScreen
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.GoToPrescriptionScreen
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.LoadPatientDetails
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleConsultSuccessEffectHandlerTest {
  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<TeleConsultSuccessScreenUiActions>()
  private val patientUuid = UUID.fromString("12111fab-1585-4d8f-982d-d3cd5b48ad1a")
  val patient = TestData.patient(uuid = patientUuid)
  private val effectHandler = TeleConsultSuccessEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      uiActions = uiActions
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun teardown() {
    testCase.dispose()
  }

  @Test
  fun `when load patient details effect is received, then load patient`() {
    // given
    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient

    // when
    testCase.dispatch(LoadPatientDetails(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientDetailsLoaded(patient))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when go to home screen effect is received, then open home screen`() {
    // when
    testCase.dispatch(GoToHomeScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).goToHomeScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when go to prescription effect is received, then open prescription screen`() {
    // when
    testCase.dispatch(GoToPrescriptionScreen(patient))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).goToPrescriptionScreen(patientUuid = patientUuid)
    verifyNoMoreInteractions(uiActions)
  }
}
