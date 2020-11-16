package org.simple.clinic.bloodsugar.unitselection

import org.simple.clinic.bloodsugar.BloodSugarUnitPreference

interface BloodSugarUnitSelectionUiActions {
  fun closeDialog()
  fun prefillBloodSugarUnitSelection(blodSugarUnitPreference: BloodSugarUnitPreference)
}
