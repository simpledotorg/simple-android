package org.simple.clinic.teleconsultlog.success

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.teleconsultlog.success.TeleConsultSuccessEffect.LoadPatientDetails
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleConsultSuccessEffectHandlerTest {
  private val patientRepository = mock<PatientRepository>()
  private val patientUuid = UUID.fromString("12111fab-1585-4d8f-982d-d3cd5b48ad1a")
  private val effectHandler = TeleConsultSuccessEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun teardown() {
    testCase.dispose()
  }

  @Test
  fun `when load patient details effect is received, then load patient`() {
    // given
    val patient = TestData.patient(uuid = patientUuid)
    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient

    // when
    testCase.dispatch(LoadPatientDetails(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientDetailsLoaded(patient))
  }

}
