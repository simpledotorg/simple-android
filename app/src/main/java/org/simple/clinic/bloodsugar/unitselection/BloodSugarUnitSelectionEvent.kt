package org.simple.clinic.bloodsugar.unitselection

import org.simple.clinic.bloodsugar.BloodSugarUnitPreference

sealed class BloodSugarUnitSelectionEvent

object BloodSugarUnitSelectionUpdated : BloodSugarUnitSelectionEvent()

data class DoneClicked(val bloodSugarUnitSelection: BloodSugarUnitPreference) : BloodSugarUnitSelectionEvent()

data class SaveBloodSugarUnitPreference(val bloodSugarUnitPreference: BloodSugarUnitPreference) : BloodSugarUnitSelectionEvent()
