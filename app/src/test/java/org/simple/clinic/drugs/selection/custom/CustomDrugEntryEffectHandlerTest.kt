package org.simple.clinic.drugs.selection.custom

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Test
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class CustomDrugEntryEffectHandlerTest {
  private val uiActions = mock<CustomDrugEntrySheetUiActions>()
  private val effectHandler = CustomDrugEntryEffectHandler(
      TestSchedulersProvider.trampoline(),
      uiActions).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when show edit frequency dialog effect is received, show edit frequency dialog`() {
    // given
    val frequency = DrugFrequency.OD

    // when
    testCase.dispatch(ShowEditFrequencyDialog(frequency))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showEditFrequencyDialog(frequency)
  }

  @Test
  fun `when set drug frequency effect is received, set drug frequency in the ui`() {
    // given
    val frequency = DrugFrequency.OD

    // when
    testCase.dispatch(SetDrugFrequency(frequency))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).setDrugFrequency(frequency)
    verifyNoMoreInteractions(uiActions)
  }
}
