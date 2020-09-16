package org.simple.clinic.teleconsultlog.prescription.patientinfo

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleconsultPatientInfoEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val effectHandler = TeleconsultPatientInfoEffectHandler(
      patientRepository = patientRepository,
      schedulersProvider = TestSchedulersProvider.trampoline()
  )
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load patient profile effect is received, then load the patient profile`() {
    // given
    val patientUuid = UUID.fromString("ca66d6a8-937f-4bbc-b43d-b9312453e4bd")
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid
    )

    whenever(patientRepository.patientProfileImmediate(patientUuid)) doReturn Optional.of(patientProfile)

    // when
    effectHandlerTestCase.dispatch(LoadPatientProfile(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientProfileLoaded(patientProfile))
  }
}
