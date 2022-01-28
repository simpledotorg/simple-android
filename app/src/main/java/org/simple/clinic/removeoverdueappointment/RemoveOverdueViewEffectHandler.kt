package org.simple.clinic.removeoverdueappointment

import org.simple.clinic.mobius.ViewEffectsHandler

class RemoveOverdueViewEffectHandler(
    private val uiActions: RemoveOverdueUiActions
) : ViewEffectsHandler<RemoveOverdueViewEffect> {
  override fun handle(viewEffect: RemoveOverdueViewEffect) {
    when (viewEffect) {
      GoBackAfterAppointmentRemoval -> uiActions.goBackAfterAppointmentRemoval()
    }
  }
}
