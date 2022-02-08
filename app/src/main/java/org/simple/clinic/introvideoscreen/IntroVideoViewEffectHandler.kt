package org.simple.clinic.introvideoscreen

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class IntroVideoViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<IntroVideoViewEffect> {
  
  override fun handle(viewEffect: IntroVideoViewEffect) {
    when(viewEffect){
      OpenVideo -> uiActions.openVideo()
    }.exhaustive()
  }
}
