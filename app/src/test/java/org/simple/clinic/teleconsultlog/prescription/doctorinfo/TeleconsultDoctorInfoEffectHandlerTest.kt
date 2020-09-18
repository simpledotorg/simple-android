package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.f2prateek.rx.preferences2.Preference
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class TeleconsultDoctorInfoEffectHandlerTest {

  private val medicalRegistrationIdPreference = mock<Preference<Optional<String>>>()
  private val effectHandler = TeleconsultDoctorInfoEffectHandler(
      medicalRegistrationIdPreference = medicalRegistrationIdPreference,
      schedulersProvider = TestSchedulersProvider.trampoline()
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load medical registration id effect is received, then load medical registration id`() {
    // given
    val medicalRegistrationId = "1234567890"

    whenever(medicalRegistrationIdPreference.get()) doReturn Optional.of(medicalRegistrationId)

    // when
    effectHandlerTestCase.dispatch(LoadMedicalRegistrationId)

    // then
    effectHandlerTestCase.assertOutgoingEvents(MedicalRegistrationIdLoaded(medicalRegistrationId))
  }
}
