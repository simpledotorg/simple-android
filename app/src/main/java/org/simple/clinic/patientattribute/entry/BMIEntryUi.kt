package org.simple.clinic.patientattribute.entry

import org.simple.clinic.patientattribute.BMIReading

interface BMIEntryUi {
  fun closeSheet(bmiReading: BMIReading?)
  fun changeFocusToHeight()
  fun changeFocusToWeight()
  fun showBMI(bmi: String)
  fun hideBMI()
}
