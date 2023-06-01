package org.simple.clinic.registration.name

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.junit.After
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import java.util.UUID

class RegistrationNameEffectHandlerTest {
  private val uiActions = mock<RegistrationNameUiActions>()
  private val effectHandler = RegistrationNameEffectHandler(
      viewEffectConsumer = RegistrationNameViewEffectHandler(uiActions)::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when prefill fields effect is received, then prefill fill user details`() {
    // given
    val ongoingRegistrationEntry = TestData.ongoingRegistrationEntry(uuid = UUID.fromString("860d4587-df76-46cc-ac7c-97a29b2e2bf3"))

    // when
    testCase.dispatch(PrefillFields(entry = ongoingRegistrationEntry))

    // then
    verify(uiActions).preFillUserDetails(ongoingRegistrationEntry)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when proceed to pin entry effect is received, then open registration pin entry screen`() {
    // given
    val ongoingRegistrationEntry = TestData.ongoingRegistrationEntry(uuid = UUID.fromString("d97e2dfb-95f0-41fd-8b5a-dfc1d3ad10db"))

    // when
    testCase.dispatch(ProceedToPinEntry(ongoingRegistrationEntry))

    // then
    verify(uiActions).openRegistrationPinEntryScreen(ongoingRegistrationEntry)
    verifyNoMoreInteractions(uiActions)
    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when validate entered name effect is received, then validate name entry`() {
    // given
    val name = "Raj"

    // when
    testCase.dispatch(ValidateEnteredName(name))

    // then
    testCase.assertOutgoingEvents(NameValidated(RegistrationNameValidationResult.Valid))
    verifyNoInteractions(uiActions)
  }


  @Test
  fun `when validate entered name effect is received and name is blank, then validate name entry`() {
    // given
    val name = ""

    // when
    testCase.dispatch(ValidateEnteredName(name))

    // then
    testCase.assertOutgoingEvents(NameValidated(RegistrationNameValidationResult.Blank))
    verifyNoInteractions(uiActions)
  }
}
