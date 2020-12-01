package org.simple.clinic.home

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.Optional
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class HomeScreenEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val effectHandler = HomeScreenEffectHandler(
      currentFacilityStream = Observable.just(TestData.facility()),
      appointmentRepository = mock(),
      patientRepository = patientRepository,
      userClock = TestUserClock(),
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = mock()
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when search patient by identifier effect is received, then search for patient`() {
    // given
    val patient = TestData.patient(
        uuid = UUID.fromString("4db4e9af-56a4-4995-958b-aeb33d80cfa5")
    )

    val identifier = TestData.identifier(
        value = "123 456",
        type = Identifier.IdentifierType.BpPassport
    )

    whenever(patientRepository.findPatientWithBusinessId(identifier.value)) doReturn Observable.just(Optional.of(patient))

    // when
    effectHandlerTestCase.dispatch(SearchPatientByIdentifier(identifier))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientSearchByIdentifierCompleted(
        patient = Optional.of(patient),
        identifier = identifier
    ))
  }
}
