package org.simple.clinic.teleconsultlog.shareprescription

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleconsultSharePrescriptionEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val effectHandler = TeleconsultSharePrescriptionEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository
  )

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler = effectHandler.build())

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load patient details effect is received, then load the patient details`() {
    // given
    val patientUuid = UUID.fromString("1cfd240c-0a05-41e2-bfa0-b20fe807aca8")
    val patient = TestData.patient(
        uuid = patientUuid
    )
    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient

    // when
    effectHandlerTestCase.dispatch(LoadPatientDetails(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientDetailsLoaded(patient))
  }
}
