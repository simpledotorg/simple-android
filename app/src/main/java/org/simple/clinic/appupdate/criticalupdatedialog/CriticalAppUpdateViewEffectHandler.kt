package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class CriticalAppUpdateViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<CriticalAppUpdateViewEffect> {

  override fun handle(viewEffect: CriticalAppUpdateViewEffect) {
    when (viewEffect) {
      is OpenHelpContactUrl -> uiActions.openContactUrl(viewEffect.contactUrl)
    }.exhaustive()
  }
}
