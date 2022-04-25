package org.simple.clinic.editpatient.deletepatient

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class DeletePatientViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<DeletePatientViewEffect> {

  override fun handle(viewEffect: DeletePatientViewEffect) {
    when (viewEffect) {
      is ShowConfirmDeleteDialog -> uiActions.showConfirmDeleteDialog(viewEffect.patientName, viewEffect.deletedReason)
      is ShowConfirmDiedDialog -> uiActions.showConfirmDiedDialog(viewEffect.patientName)
    }.exhaustive()
  }
}
