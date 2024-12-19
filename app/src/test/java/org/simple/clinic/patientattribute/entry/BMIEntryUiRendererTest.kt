package org.simple.clinic.patientattribute.entry

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.simple.clinic.patientattribute.BMIReading
import java.util.UUID

class BMIEntryUiRendererTest {
  private val ui = mock<BMIEntryUi>()
  private val uiRenderer = BMIEntryUiRenderer(ui)
  private val patientUuid = UUID.fromString("d6dd1708-9061-4c6c-b5c7-47b132198862")
  private val defaultModel = BMIEntryModel
      .default(patientUuid)

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
    val height = "177"
    val weight = "68"
    val reading = BMIReading(height, weight)
    val bmi = reading.calculateBMI().toString()

    // when
    uiRenderer.render(defaultModel.heightChanged(height).weightChanged(weight))

    // then
    verify(ui).showBMI(bmi)
    verifyNoMoreInteractions(ui)
  }
}
