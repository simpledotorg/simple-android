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

  @Test
  fun `when set drug name effect is received with non null values, construct drug name and set it in the ui`() {
    // given
    val drugName = "Amolodipine"
    val dosage = "15mg"
    val frequency = DrugFrequency.OD
    val updatedDrugName = "Amolodipine, 15mg, OD"

    // when
    testCase.dispatch(SetSheetTitle(drugName, dosage, frequency))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).setSheetTitle(updatedDrugName)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when set drug name effect is received with null values, construct drug name and set it in the ui`() {
    // given
    val drugName = "Amolodipine"
    val updatedDrugName = "Amolodipine"

    // when
    testCase.dispatch(SetSheetTitle(drugName, null, null))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).setSheetTitle(updatedDrugName)
    verifyNoMoreInteractions(uiActions)
  }
}
