package org.simple.clinic.summary.nextappointment

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class NextAppointmentViewEffectHandler(
    private val uiActions: NextAppointmentUiActions
) : ViewEffectsHandler<NextAppointmentViewEffect> {

  override fun handle(viewEffect: NextAppointmentViewEffect) {
    when (viewEffect) {
      is OpenScheduleAppointmentSheet -> uiActions.openScheduleAppointmentSheet(viewEffect.patientUuid)
    }.exhaustive()
  }
}
