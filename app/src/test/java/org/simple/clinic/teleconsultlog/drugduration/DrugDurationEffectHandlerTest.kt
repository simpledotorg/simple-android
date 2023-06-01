package org.simple.clinic.teleconsultlog.drugduration

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class DrugDurationEffectHandlerTest {

  private val uiActions = mock<DrugDurationUiActions>()
  private val effectHandler = DrugDurationEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  )
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when save drug duration effect is received, then save duration and close sheet`() {
    // given
    val duration = "20"

    // when
    effectHandlerTestCase.dispatch(SaveDrugDuration(duration.toInt()))

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).saveDrugDuration(duration.toInt())
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when prefill drug duration effect is received, then prefill drug duration`() {
    // given
    val duration = "35"

    // when
    effectHandlerTestCase.dispatch(PrefillDrugDuration(duration))

    // then
    verify(uiActions).prefillDrugDuration(duration)
    verifyNoMoreInteractions(uiActions)
  }
}
