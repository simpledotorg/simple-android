package org.simple.clinic.introvideoscreen

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class IntroVideoEffectHandlerTest {

  private val uiActions = mock<UiActions>()
  private val effectHandler = IntroVideoEffectHandler(
      uiActions = uiActions,
      schedulersProvider = TrampolineSchedulersProvider()
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @Test
  fun `when open video effect is received, then open the video`() {
    // when
    effectHandlerTestCase.dispatch(OpenVideo)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openVideo()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open home effect is received, then open home screen`() {
    // when
    effectHandlerTestCase.dispatch(OpenHome)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).openHome()
    verifyNoMoreInteractions(uiActions)
  }

  @After
  fun teardown() {
    effectHandlerTestCase.dispose()
  }
}
