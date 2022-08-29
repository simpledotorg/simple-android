package org.simple.clinic.home.help

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class HelpScreenViewEffectHandler(
    private val uiActions: HelpScreenUiActions
) : ViewEffectsHandler<HelpScreenViewEffect> {

  override fun handle(viewEffect: HelpScreenViewEffect) {
    when (viewEffect) {
      ShowLoadingView -> uiActions.showLoadingView()
    }.exhaustive()
  }
}
