package org.simple.clinic.scanid

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class ScanSimpleIdEffectHandlerTest {

  @Test
  fun `when the send scanned identifier result is received, the scanned identifier result must be sent`() {
    // given
    val uiActions = mock<ScanSimpleIdUiActions>()
    val testCase = EffectHandlerTestCase(ScanSimpleIdEffectHandler(
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    ).build())

    // when
    val identifier = TestData.identifier(
        value = "ec08a1c4-4e57-4882-8292-d33205e1f098",
        type = BpPassport
    )
    testCase.dispatch(SendScannedIdentifierResult(identifier))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).sendScannedId(identifier)
    verifyNoMoreInteractions(uiActions)
    testCase.dispose()
  }
}
