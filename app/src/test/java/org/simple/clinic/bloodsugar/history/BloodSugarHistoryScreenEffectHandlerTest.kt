package org.simple.clinic.bloodsugar.history

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class BloodSugarHistoryScreenEffectHandlerTest {
  private val patientRepository = mock<PatientRepository>()
  private val patientUuid = UUID.fromString("1d695883-54cf-4cf0-8795-43f83a0c3f02")
  private val effectHandler = BloodSugarHistoryScreenEffectHandler(
      patientRepository,
      TrampolineSchedulersProvider()
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load patient effect is received, then load patient`() {
    // given
    val patient = PatientMocker.patient(uuid = patientUuid)
    whenever(patientRepository.patient(patientUuid)) doReturn Observable.just<Optional<Patient>>(Just(patient))

    // when
    testCase.dispatch(LoadPatient(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientLoaded(patient))
  }
}
