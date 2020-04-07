package org.simple.clinic.patientcontact

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class PatientContactEffectHandlerTest {

  @Test
  fun `when the load patient profile effect is received, the patient profile must be loaded`() {
    // given
    val patientUuid = UUID.fromString("8a490518-a016-4818-b725-22c25dec310b")
    val patientRepository = mock<PatientRepository>()
    val patientProfile = TestData.patientProfile(patientUuid = patientUuid)
    whenever(patientRepository.patientProfileImmediate(patientUuid)) doReturn Just(patientProfile)

    val effectHandler = PatientContactEffectHandler(patientRepository, TrampolineSchedulersProvider()).build()
    val testCase = EffectHandlerTestCase(effectHandler)

    // when
    testCase.dispatch(LoadPatientProfile(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientProfileLoaded(patientProfile))
    testCase.dispose()
  }
}
