package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase

class TeleconsultRecordEffectHandlerTest {

  private val uiActions = mock<UiActions>()
  private val effectHandler = TeleconsultRecordEffectHandler(uiActions)
      .build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when go back effect is received, then go back to previous screen`() {
    // when
    effectHandlerTestCase.dispatch(GoBack)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).goBackToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }
}
