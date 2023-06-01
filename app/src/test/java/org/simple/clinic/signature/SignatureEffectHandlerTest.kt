package org.simple.clinic.signature

import android.graphics.Bitmap
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class SignatureEffectHandlerTest {

  private val uiActions = mock<SignatureUiActions>()
  private val signatureRepository = mock<SignatureRepository>()
  private val effectHandler = SignatureEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      signatureRepository = signatureRepository,
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

  @Test
  fun `when load signature effect is received, then load signature`() {
    // given
    val signatureBitmap = mock<Bitmap>()

    whenever(signatureRepository.getSignatureBitmap()) doReturn signatureBitmap

    // when
    effectHandlerTestCase.dispatch(LoadSignatureBitmap)

    // then
    effectHandlerTestCase.assertOutgoingEvents(SignatureBitmapLoaded(signatureBitmap))

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when set signature bitmap effect is received, then set signature bitmap`() {
    // given
    val signatureBitmap = mock<Bitmap>()

    // when
    effectHandlerTestCase.dispatch(SetSignatureBitmap(signatureBitmap))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).setSignatureBitmap(signatureBitmap)
    verifyNoMoreInteractions(uiActions)
  }
}
