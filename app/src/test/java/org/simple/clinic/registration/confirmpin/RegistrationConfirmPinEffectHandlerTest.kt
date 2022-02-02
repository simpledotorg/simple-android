package org.simple.clinic.registration.confirmpin

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class RegistrationConfirmPinEffectHandlerTest {
  private val uiActions = mock<RegistrationConfirmPinUiActions>()
  private val effectHandler = RegistrationConfirmPinEffectHandler(
      uiActions = uiActions,
      schedulers = TestSchedulersProvider.trampoline(),
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
}
