package org.simple.clinic.summary.addcholesterol

import org.simple.clinic.mobius.ViewRenderer

class CholesterolEntryUiRenderer(
    private val ui: CholesterolEntryUi
) : ViewRenderer<CholesterolEntryModel> {

  override fun render(model: CholesterolEntryModel) {
    when (model.cholesterolSaveState) {
      CholesterolEntrySaveState.SAVING_CHOLESTEROL -> ui.showProgress()
      CholesterolEntrySaveState.NOT_SAVING_CHOLESTEROL -> ui.hideProgress()
    }
  }
}
