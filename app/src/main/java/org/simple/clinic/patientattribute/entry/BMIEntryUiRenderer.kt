package org.simple.clinic.patientattribute.entry

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.patientattribute.BMIReading

class BMIEntryUiRenderer(
    val ui: BMIEntryUi
) : ViewRenderer<BMIEntryModel> {

  override fun render(model: BMIEntryModel) {
    if (model.height.isNotEmpty() && model.weight.isNotEmpty()) {
      val bmi = BMIReading(
          height = model.height.toFloat(),
          weight = model.weight.toFloat()
      ).calculateBMI()

      ui.showBMI(bmi.toString())
    } else {
      ui.hideBMI()
    }
  }
}
