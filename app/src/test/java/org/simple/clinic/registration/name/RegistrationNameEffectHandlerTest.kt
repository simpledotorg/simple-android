package org.simple.clinic.registration.name

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class RegistrationNameEffectHandlerTest {
  private val uiActions = mock<RegistrationNameUiActions>()
  private val effectHandler = RegistrationNameEffectHandler(
      schedulers = TestSchedulersProvider.trampoline(),
      uiActions = uiActions,
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
}
