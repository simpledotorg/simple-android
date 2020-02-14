package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarMeasurementType
import org.simple.clinic.bloodsugar.Fasting
import org.simple.clinic.bloodsugar.PostPrandial
import org.simple.clinic.bloodsugar.Random
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
  }

  private fun setupUi(openAs: OpenAs) {
    when (openAs) {
      is New -> {
        ui.hideRemoveButton()
        showEnterNewBloodSugarTitle(openAs.measurementType)
      }
      is Update -> {
        ui.showRemoveButton()
        showEditBloodSugarTitle(openAs.measurementType)
      }
    }
  }

  private fun showEnterNewBloodSugarTitle(measurementType: BloodSugarMeasurementType) {
    with(ui) {
      when (measurementType) {
        is Random -> showRandomBloodSugarTitle()
        is PostPrandial -> showPostPrandialBloodSugarTitle()
        is Fasting -> showFastingBloodSugarTitle()
      }
    }
  }

  private fun showEditBloodSugarTitle(measurementType: BloodSugarMeasurementType) {
    with(ui) {
      when (measurementType) {
        is Random -> showEditRadomBloodSugarTitle()
        is PostPrandial -> showEditPostPrandialBloodSugarTitle()
        is Fasting -> showEditFastingBloodSugarTitle()
      }
    }
  }
}
