package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class TeleconsultRecordEffectHandlerTest {

  private val uiActions = mock<UiActions>()
  private val effectHandler = TeleconsultRecordEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()
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

  @Test
  fun `when navigate to teleconsult success screen effect is received, then navigate to teleconsult success screen`() {
    // given
    val teleconsultRecordId = UUID.fromString("4d30c778-dec0-48f0-90a0-acf4d568eb6e")

    // when
    effectHandlerTestCase.dispatch(NavigateToTeleconsultSuccess(teleconsultRecordId))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).navigateToTeleconsultSuccessScreen(teleconsultRecordId)
    verifyNoMoreInteractions(uiActions)
  }
}
