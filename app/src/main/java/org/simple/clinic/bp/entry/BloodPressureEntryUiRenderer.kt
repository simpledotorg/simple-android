package org.simple.clinic.bp.entry

import org.simple.clinic.bp.entry.BloodPressureSaveState.SAVING_BLOOD_PRESSURE
import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.bp.entry.OpenAs.Update
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.util.ValueChangedCallback

class BloodPressureEntryUiRenderer(
    val ui: BloodPressureEntryUi
) : ViewRenderer<BloodPressureEntryModel> {
  private val openAsValueChangedCallback = ValueChangedCallback<OpenAs>()

  override fun render(model: BloodPressureEntryModel) {
    openAsValueChangedCallback.pass(model.openAs) { setupUi(it) }
    manageProgress(model)
  }

  private fun manageProgress(model: BloodPressureEntryModel) {
    if (model.bloodPressureSaveState == SAVING_BLOOD_PRESSURE) {
      ui.showProgress()
    } else {
      ui.hideProgress()
    }
  }

  private fun setupUi(openAs: OpenAs) {
    when (openAs) {
      is New -> with(ui) {
        hideRemoveBpButton()
        showEnterNewBloodPressureTitle()
      }

      is Update -> with(ui) {
        showRemoveBpButton()
        showEditBloodPressureTitle()
      }
    }
  }
}
