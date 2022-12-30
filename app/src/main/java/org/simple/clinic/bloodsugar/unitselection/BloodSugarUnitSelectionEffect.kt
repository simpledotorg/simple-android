package org.simple.clinic.bloodsugar.unitselection

import org.simple.clinic.bloodsugar.BloodSugarUnitPreference

sealed class BloodSugarUnitSelectionEffect

data class SaveBloodSugarUnitSelection(val bloodSugarUnitSelection: BloodSugarUnitPreference) : BloodSugarUnitSelectionEffect()

sealed class BloodSugarUnitSelectionViewEffect : BloodSugarUnitSelectionEffect()

object CloseDialog : BloodSugarUnitSelectionViewEffect()

data class PreFillBloodSugarUnitSelected(val bloodSugarUnitPreference: BloodSugarUnitPreference) : BloodSugarUnitSelectionViewEffect()
