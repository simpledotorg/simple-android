package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.mobius.ViewRenderer

class CriticalAppUpdateUiRenderer(
    private val ui: CriticalAppUpdateUi
) : ViewRenderer<CriticalAppUpdateModel> {

  override fun render(model: CriticalAppUpdateModel) {
    if (model.hasHelpContact) {
      ui.showHelp()
    }
  }
}
