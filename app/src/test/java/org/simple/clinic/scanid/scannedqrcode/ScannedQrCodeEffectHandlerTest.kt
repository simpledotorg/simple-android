package org.simple.clinic.scanid.scannedqrcode

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class ScannedQrCodeEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<ScannedQrCodeUiActions>()
  private val effectHandler = ScannedQrCodeEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      patientRepository = patientRepository,
      viewEffectsConsumer = ScannedQrCodeViewEffectHandler(uiActions)::handle
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when save new on going patient entry effect is received, then save the ongoing entry`() {
    // given
    val ongoingNewPatientEntry = TestData.ongoingPatientEntry(
        identifier = Identifier("1111111", Identifier.IdentifierType.BpPassport)
    )

    // when
    effectHandlerTestCase.dispatch(SaveNewOngoingPatientEntry(ongoingNewPatientEntry))

    // then
    effectHandlerTestCase.assertOutgoingEvents(NewOngoingPatientEntrySaved)
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when send scanned qr code passport result effect is received, then send add to existing patient`() {
    // when
    effectHandlerTestCase.dispatch(SendBlankScannedQrCodeResult(AddToExistingPatient))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).sendScannedQrCodeResult(AddToExistingPatient)
    verifyNoMoreInteractions(uiActions)
  }
}
