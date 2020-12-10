package org.simple.clinic.home

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class HomeScreenEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<HomeScreenUiActions>()

  private val effectHandler = HomeScreenEffectHandler(
      currentFacilityStream = Observable.just(TestData.facility()),
      appointmentRepository = mock(),
      patientRepository = patientRepository,
      userClock = TestUserClock(),
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when open short code search screen effect is received, then open the screen`() {
    // given
    val shortCode = "123456"

    // when
    effectHandlerTestCase.dispatch(OpenShortCodeSearchScreen(shortCode))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openShortCodeSearchScreen(shortCode)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open patient search screen effect is received, then open the screen`() {
    // given
    val identifier = TestData.identifier(
        value = "123 456",
        type = Identifier.IdentifierType.BpPassport
    )

    // when
    effectHandlerTestCase.dispatch(OpenPatientSearchScreen(identifier))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openPatientSearchScreen(identifier)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open patient summary effect is received, then open the screen`() {
    // given
    val patientUuid = UUID.fromString("fc14d003-0d49-422a-b0e6-34a372e78544")

    // when
    effectHandlerTestCase.dispatch(OpenPatientSummary(patientUuid))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openPatientSummary(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }
}
