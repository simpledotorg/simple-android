package org.simple.clinic.scanid

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
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class ScanSimpleIdEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<ScanSimpleIdUiActions>()
  private val testCase = EffectHandlerTestCase(ScanSimpleIdEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      uiActions = uiActions
  ).build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the send scanned identifier result is received, the scanned identifier result must be sent`() {
    // when
    val scannedId = ScannedId(TestData.identifier(
        value = "ec08a1c4-4e57-4882-8292-d33205e1f098",
        type = BpPassport
    ))
    testCase.dispatch(SendScannedIdentifierResult(scannedId))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).sendScannedId(scannedId)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when search for patient by identifier effect is received, then search for patient`() {
    // given
    val patient = TestData.patient(
        uuid = UUID.fromString("4db4e9af-56a4-4995-958b-aeb33d80cfa5")
    )

    val identifier = TestData.identifier(
        value = "123 456",
        type = BpPassport
    )

    whenever(patientRepository.findPatientWithBusinessId(identifier.value)) doReturn Observable.just(Optional.of(patient))

    // when
    testCase.dispatch(SearchPatientByIdentifier(identifier))

    // then
    testCase.assertOutgoingEvents(PatientSearchByIdentifierCompleted(
        patient = Optional.of(patient),
        identifier = identifier
    ))
    verifyZeroInteractions(uiActions)
  }
}
