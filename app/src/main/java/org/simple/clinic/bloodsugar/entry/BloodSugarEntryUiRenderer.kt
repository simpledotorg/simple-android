package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.entry.OpenAs.New
import org.simple.clinic.bloodsugar.entry.OpenAs.Update
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class BloodSugarEntryUiRenderer(
    private val ui: BloodSugarEntryUi
) : ViewRenderer<BloodSugarEntryModel> {
  private val openAsValueChangedCallback = ValueChangedCallback<OpenAs>()

  override fun render(model: BloodSugarEntryModel) {
    openAsValueChangedCallback.pass(model.openAs) { setupUi(it) }
    manageProgress(model)
    manageBloodSugarUnitPreferenceButtonText(model)
  }

  private fun manageBloodSugarUnitPreferenceButtonText(model: BloodSugarEntryModel) {
    if (model.bloodSugarUnitPreference == BloodSugarUnitPreference.Mmol)
      ui.setBloodSugarUnitPreferenceLabelToMmol()
    else
      ui.setBloodSugarUnitPreferenceLabelToMg()
  }

  private fun manageProgress(model: BloodSugarEntryModel) {
    if (model.bloodSugarSaveState == BloodSugarSaveState.SAVING_BLOOD_SUGAR) {
      ui.showProgress()
    } else {
      ui.hideProgress()
    }
  }

  private fun setupUi(openAs: OpenAs) {
    when (openAs) {
      is New -> {
        ui.hideRemoveButton()
        ui.showEntryTitle(openAs.measurementType)
      }
      is Update -> {
        ui.showRemoveButton()
        ui.showEditTitle(openAs.measurementType)
      }
    }
  }
}
