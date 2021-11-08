package org.simple.clinic.home

import org.simple.clinic.mobius.ViewEffectsHandler

class HomeScreenViewEffectHandler(
    private val uiActions: HomeScreenUiActions
) : ViewEffectsHandler<HomeScreenViewEffect> {

  override fun handle(viewEffect: HomeScreenViewEffect) {
    // nothing, it does nothing.
  }
}
