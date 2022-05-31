package org.simple.clinic.registration.confirmpin

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.After
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.DoesNotMatchEnteredPin
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinValidationResult.Valid
import java.util.UUID

class RegistrationConfirmPinEffectHandlerTest {
  private val uiActions = mock<RegistrationConfirmPinUiActions>()
  private val effectHandler = RegistrationConfirmPinEffectHandler(
      viewEffectsConsumer = RegistrationConfirmPinViewEffectHandler(uiActions)::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when clear pin effect is received, then clear pin`() {
    // when
    testCase.dispatch(ClearPin)

    // then
    verify(uiActions).clearPin()
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when open facility selection screen effect is received, then open facility selection screen`() {
    // given
    val ongoingRegistrationEntry = TestData.ongoingRegistrationEntry(uuid = UUID.fromString("79ae24ef-6ca2-4380-882d-050b271928da"))

    // when
    testCase.dispatch(OpenFacilitySelectionScreen(ongoingRegistrationEntry))

    // then
    verify(uiActions).openFacilitySelectionScreen(ongoingRegistrationEntry)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when go back to pin entry effect is received, then go back to pin screen`() {
    // given
    val ongoingRegistrationEntry = TestData.ongoingRegistrationEntry(uuid = UUID.fromString("79ae24ef-6ca2-4380-882d-050b271928da"))

    // when
    testCase.dispatch(GoBackToPinEntry(ongoingRegistrationEntry))

    // then
    verify(uiActions).goBackToPinScreen(ongoingRegistrationEntry)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when validate pin confirmation effect is received and the pin is valid, then check if confirmed is valid`() {
    // given
    val pinConfirmation = "1111"
    val ongoingRegistrationEntry = TestData.ongoingRegistrationEntry(
        uuid = UUID.fromString("79ae24ef-6ca2-4380-882d-050b271928da"),
        pin = pinConfirmation
    )

    // when
    testCase.dispatch(ValidatePinConfirmation(pinConfirmation, ongoingRegistrationEntry))

    // then
    testCase.assertOutgoingEvents(PinConfirmationValidated(Valid))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when validate pin confirmation effect is received and the pin is invalid, then check if confirmed is valid`() {
    // given
    val pinConfirmation = "1111"
    val ongoingRegistrationEntry = TestData.ongoingRegistrationEntry(
        uuid = UUID.fromString("79ae24ef-6ca2-4380-882d-050b271928da"),
        pin = "1222"
    )

    // when
    testCase.dispatch(ValidatePinConfirmation(pinConfirmation, ongoingRegistrationEntry))

    // then
    testCase.assertOutgoingEvents(PinConfirmationValidated(DoesNotMatchEnteredPin))
    verifyZeroInteractions(uiActions)
  }
}
