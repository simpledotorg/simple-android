package org.simple.clinic.summary.linkId

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class LinkIdWithPatientViewEffectHandler(
    private val uiActions: LinkIdWithPatientUiActions
) : ViewEffectsHandler<LinkIdWithPatientViewEffect> {

  override fun handle(viewEffect: LinkIdWithPatientViewEffect) {
    when (viewEffect) {
      CloseSheetWithOutIdLinked -> uiActions.closeSheetWithoutIdLinked()
      CloseSheetWithLinkedId -> uiActions.closeSheetWithIdLinked()
    }.exhaustive()
  }
}
