package org.simple.clinic.patientattribute.entry

import org.junit.After
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patientattribute.BMIReading
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class BMIEntryEffectHandlerTest {
  private val ui = mock<BMIEntryUi>()

  private val effectHandler = BMIEntryEffectHandler(
      ui = ui,
      schedulersProvider = TestSchedulersProvider.trampoline(),
  ).build()

  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun teardown() {
    testCase.dispose()
  }

  @Test
  fun `when close sheet view effect is received, then close sheet`() {
    //given
    val bmiReading = BMIReading(height = 177f, weight = 63f)
    //when
    testCase.dispatch(CloseSheet(bmiReading))

    //then
    testCase.assertNoOutgoingEvents()
    verify(ui).closeSheet(bmiReading)
  }

  @Test
  fun `when change focus to height view effect is received, then change the focus to height`() {
    //when
    testCase.dispatch(ChangeFocusToHeight)

    //then
    testCase.assertNoOutgoingEvents()
    verify(ui).changeFocusToHeight()
  }

  @Test
  fun `when change focus to weight view effect is received, then change the focus to weight`() {
    //when
    testCase.dispatch(ChangeFocusToWeight)

    //then
    testCase.assertNoOutgoingEvents()
    verify(ui).changeFocusToWeight()
  }
}
