package org.simple.clinic.registerorlogin

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class AuthenticationEffectHandlerTest {

  @Test
  fun `when receiving the open country selection screen effect, open the country selection screen`() {
    // given
    val uiActions = mock<AuthenticationUiActions>()
    val effectHandler = AuthenticationEffectHandler(
        schedulers = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    ).build()
    val testCase = EffectHandlerTestCase(effectHandler)

    // when
    testCase.dispatch(OpenCountrySelectionScreen)

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openCountrySelectionScreen()
    verifyNoMoreInteractions(uiActions)
    testCase.dispose()
  }
}
