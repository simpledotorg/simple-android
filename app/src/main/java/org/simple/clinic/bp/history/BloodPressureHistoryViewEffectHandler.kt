package org.simple.clinic.bp.history

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class BloodPressureHistoryViewEffectHandler(
    private val uiActions: BloodPressureHistoryScreenUiActions
) : ViewEffectsHandler<BloodPressureHistoryViewEffect> {

  override fun handle(viewEffect: BloodPressureHistoryViewEffect) {
    when (viewEffect) {
      is OpenBloodPressureEntrySheet -> uiActions.openBloodPressureEntrySheet(viewEffect.patientUuid)
      is OpenBloodPressureUpdateSheet -> uiActions.openBloodPressureUpdateSheet(viewEffect.bloodPressureMeasurement.uuid)
    }.exhaustive()
  }
}
