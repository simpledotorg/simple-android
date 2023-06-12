package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import java.time.LocalDate
import java.util.UUID

interface BloodSugarEntryUi {
  fun showRemoveButton()
  fun hideRemoveButton()
  fun showEntryTitle(measurementType: BloodSugarMeasurementType)
  fun showEditTitle(measurementType: BloodSugarMeasurementType)
  fun showProgress()
  fun hideProgress()
  fun setBloodSugarUnitPreferenceLabelToMmol()
  fun setBloodSugarUnitPreferenceLabelToMg()
  fun showBloodSugarUnitPreferenceButton()
  fun hideBloodSugarUnitPreferenceButton()
  fun showBloodSugarUnitPreferenceLabel()
  fun hideBloodSugarUnitPreferenceLabel()
  fun decimalOrNumericBloodSugarInputType()
  fun numericBloodSugarInputType()
  fun setLabelForHbA1c()
  fun setLabelForUnknown()
}
