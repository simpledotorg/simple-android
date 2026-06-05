package org.simple.clinic.patientattribute.entry

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.simple.clinic.patientattribute.BMIReading

class BMIEntryUiRendererTest {
  private val ui = mock<BMIEntryUi>()
  private val uiRenderer = BMIEntryUiRenderer(ui)
  private val defaultModel = BMIEntryModel
      .default(BMIReading(height = 177f, weight = 63f))

  @Test
  fun `when the sheet is show for a new entry, then hide bmi`() {
    // when
    uiRenderer.render(defaultModel)

    // then
    verify(ui).hideBMI()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the height and weight are entered, then show bmi`() {
    //given
    val height = 177f
    val weight = 68f
    val reading = BMIReading(height, weight)
    val bmi = reading.calculateBMI().toString()

    // when
    uiRenderer.render(defaultModel.heightChanged(height.toString()).weightChanged(weight.toString()))

    // then
    verify(ui).showBMI(bmi)
    verifyNoMoreInteractions(ui)
  }
}
