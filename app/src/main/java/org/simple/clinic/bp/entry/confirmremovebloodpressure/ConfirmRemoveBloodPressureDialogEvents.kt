package org.simple.clinic.bp.entry.confirmremovebloodpressure

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class ConfirmRemoveBloodPressureDialogCreated(val bloodPressureMeasurementUuid: UUID): UiEvent {
  override val analyticsName = "Confirm Remove Blood Pressure:Dialog Created"
}

object ConfirmRemoveBloodPressureDialogRemoveClicked : UiEvent {
  override val analyticsName = "Confirm Remove Blood Pressure:Remove Clicked"
}
