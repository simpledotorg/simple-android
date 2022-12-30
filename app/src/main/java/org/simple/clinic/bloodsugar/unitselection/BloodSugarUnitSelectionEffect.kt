package org.simple.clinic.bloodsugar.unitselection

import org.simple.clinic.bloodsugar.BloodSugarUnitPreference

sealed class BloodSugarUnitSelectionEffect

data class SaveBloodSugarUnitSelection(val bloodSugarUnitSelection: BloodSugarUnitPreference) : BloodSugarUnitSelectionEffect()

data class PreFillBloodSugarUnitSelected(val bloodSugarUnitPreference: BloodSugarUnitPreference) : BloodSugarUnitSelectionEffect()

sealed class BloodSugarUnitSelectionViewEffect : BloodSugarUnitSelectionEffect()

object CloseDialog : BloodSugarUnitSelectionViewEffect()
