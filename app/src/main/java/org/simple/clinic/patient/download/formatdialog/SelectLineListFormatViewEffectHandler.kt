package org.simple.clinic.patient.download.formatdialog

import org.simple.clinic.mobius.ViewEffectsHandler

class SelectLineListFormatViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<SelectLineListFormatViewEffect> {

  override fun handle(viewEffect: SelectLineListFormatViewEffect) {
    when (viewEffect) {
      Dismiss -> uiActions.dismiss()
    }
  }
}
