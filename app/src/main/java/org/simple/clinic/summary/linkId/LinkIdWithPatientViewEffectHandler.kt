package org.simple.clinic.summary.linkId

import org.simple.clinic.mobius.ViewEffectsHandler

class LinkIdWithPatientViewEffectHandler(
    private val uiActions: LinkIdWithPatientUiActions
) : ViewEffectsHandler<LinkIdWithPatientViewEffect> {

  override fun handle(viewEffect: LinkIdWithPatientViewEffect) {
    // to be, or not to be, that is the question
  }
}
