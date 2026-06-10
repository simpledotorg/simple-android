package org.simple.clinic.patientattribute.entry

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.simple.clinic.patientattribute.BMIReading

class BMIEntryUiRendererTest {
  private val ui = mock<BMIEntryUi>()
  private val uiRenderer = BMIEntryUiRenderer(ui)

  @Test
  fun `when the sheet is shown for a new entry, then hide bmi`() {
    // given
    val model = BMIEntryModel.default(null)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).updateHeight("")
    verify(ui).updateWeight("")
    verify(ui).hideBMI()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the height and weight are entered, then show bmi`() {
    // given
    val height = 177f
    val weight = 68f

    val reading = BMIReading(height, weight)
    val bmi = reading.calculateBMI().toString()

    val model = BMIEntryModel.default(null)
        .heightChanged(height.toInt().toString())
        .weightChanged(weight.toInt().toString())

    // when
    uiRenderer.render(model)

    // then
    verify(ui).updateHeight("177")
    verify(ui).updateWeight("68")
    verify(ui).showBMI(bmi)
    verifyNoMoreInteractions(ui)
  }
}
