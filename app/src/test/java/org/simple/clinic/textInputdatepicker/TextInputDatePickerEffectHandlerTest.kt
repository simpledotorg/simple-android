package org.simple.clinic.textInputdatepicker

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.DismissSheet
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.HideDateErrorMessage
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class TextInputDatePickerEffectHandlerTest {

  private val uiActions = mock<TextInputDatePickerUiActions>()
  private val effectHandler = TextInputDatePickerEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()

  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)
  
  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when close sheet effect is received, then dismiss the sheet`() {
    // when
    effectHandlerTestCase.dispatch(DismissSheet)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).dismissSheet()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when hide date error message is received, then hide the error text`() {
    // when
    effectHandlerTestCase.dispatch(HideDateErrorMessage)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()
    verify(uiActions).hideErrorMessage()
    verifyNoMoreInteractions(uiActions)
  }

}
