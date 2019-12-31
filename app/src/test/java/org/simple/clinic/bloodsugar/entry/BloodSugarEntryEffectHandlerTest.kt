package org.simple.clinic.bloodsugar.entry

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider

class BloodSugarEntryEffectHandlerTest {

  private val ui = mock<BloodSugarEntryUi>()

  private val effectHandler = BloodSugarEntryEffectHandler(
      ui,
      TrampolineSchedulersProvider()
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `blood sugar error message must be hidden when hide blood sugar error message effect is received`() {
    // when
    testCase.dispatch(HideBloodSugarErrorMessage)

    // then
    verify(ui).hideBloodSugarErrorMessage()
    verifyNoMoreInteractions(ui)
  }
}
