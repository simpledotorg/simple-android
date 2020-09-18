package org.simple.clinic.signature

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class SignatureEffectHandlerTest {

  private val uiActions = mock<SignatureUiActions>()
  private val effectHandler = SignatureEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      signatureRepository = mock(),
      uiActions = uiActions
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @Test
  fun `signature drawn on the screen should be cleared when clear signature button is clicked`() {
    // when
    effectHandlerTestCase.dispatch(ClearSignature)

    // then
    verify(uiActions).clearSignature()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `close the screen when the signature is saved`() {
    // when
    effectHandlerTestCase.dispatch(CloseScreen)

    // then
    verify(uiActions).closeScreen()
    verifyNoMoreInteractions(uiActions)
  }

}
