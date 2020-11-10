package org.simple.clinic.home.patients

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.util.Optional
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class PatientsEffectHandlerTest {

  private val uiActions = mock<PatientsTabUiActions>()

  private val patientRepository = mock<PatientRepository>()

  private val testCase = EffectHandlerTestCase(PatientsEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      refreshCurrentUser = mock(),
      userSession = mock(),
      utcClock = TestUtcClock(),
      userClock = TestUserClock(),
      checkAppUpdate = mock(),
      patientRepository = patientRepository,
      approvalStatusUpdatedAtPref = mock(),
      hasUserDismissedApprovedStatusPref = mock(),
      numberOfPatientsRegisteredPref = mock(),
      appUpdateDialogShownAtPref = mock(),
      uiActions = uiActions
  ).build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the open short code search screen effect is received, the short code search screen must be opened`() {
    // when
    val shortCode = "1234567"
    testCase.dispatch(OpenShortCodeSearchScreen(shortCode))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openShortCodeSearchScreen(shortCode)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the search patient by identifier effect is received, the patient must be searched`() {
    // given
    val identifier = TestData.identifier("afa2d568-771e-4b0e-95f0-fbd7b4fbeaaf", BpPassport)
    val patient = TestData.patient(uuid = UUID.fromString("a83bf77e-3b21-4784-93dd-4de84ad14c1f"))
    whenever(patientRepository.findPatientWithBusinessId(identifier.value)).doReturn(Observable.just(Optional.of(patient)))

    // when
    testCase.dispatch(SearchPatientByIdentifier(identifier))

    // then
    val expected = PatientSearchByIdentifierCompleted(
        foundPatient = Optional.of(patient),
        searchedIdentifier = identifier
    )
    testCase.assertOutgoingEvents(expected)
    verifyZeroInteractions(uiActions)
  }
}
