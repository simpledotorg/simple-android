package org.simple.clinic.bp.entry

import org.simple.clinic.bp.entry.OpenAs.New
import org.simple.clinic.mobius.ViewRenderer

class BloodPressureEntryViewRenderer(
    val ui: BloodPressureEntryUi
) : ViewRenderer<BloodPressureEntryModel> {
  override fun render(model: BloodPressureEntryModel) {
    setupUi(model.openAs)
  }

  private fun setupUi(openAs: OpenAs) {
    when (openAs) {
      is New -> with(ui) {
        hideRemoveBpButton()
        showEnterNewBloodPressureTitle()
      }
    }
  }
}
