package org.simple.clinic.teleconsultlog.drugduration

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
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
}
