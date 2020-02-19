package org.simple.clinic.bloodsugar.entry

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
        ui.showEntryTitle(openAs.measurementType)
      }
      is Update -> {
        ui.showRemoveButton()
        ui.showEditTitle(openAs.measurementType)
      }
    }
  }
}
