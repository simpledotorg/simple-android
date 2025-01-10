package org.simple.clinic.patientattribute.entry

interface BMIEntryUi {
  fun closeSheet()
  fun changeFocusToHeight()
  fun changeFocusToWeight()
  fun showBMI(bmi: String)
  fun hideBMI()
}
