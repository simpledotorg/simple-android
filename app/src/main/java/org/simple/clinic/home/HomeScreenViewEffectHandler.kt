package org.simple.clinic.home

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class HomeScreenViewEffectHandler(
    private val uiActions: HomeScreenUiActions
) : ViewEffectsHandler<HomeScreenViewEffect> {

  override fun handle(viewEffect: HomeScreenViewEffect) {
    when (viewEffect) {
      OpenFacilitySelection -> uiActions.openFacilitySelection()
    }.exhaustive()
  }
}
