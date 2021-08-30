package org.simple.clinic.scheduleappointment

import org.simple.clinic.mobius.ViewEffectsHandler

class ScheduleAppointmentViewEffectHandler(
    private val uiActions: ScheduleAppointmentUiActions
) : ViewEffectsHandler<ScheduleAppointmentViewEffect> {

  override fun handle(viewEffect: ScheduleAppointmentViewEffect) {
    when (viewEffect) {
      is ShowDatePicker -> uiActions.showManualDateSelector(viewEffect.selectedDate)
      CloseSheet -> uiActions.closeSheet()
      is GoToTeleconsultStatusSheet -> uiActions.openTeleconsultStatusSheet(viewEffect.teleconsultRecordUuid)
    }
  }
}
