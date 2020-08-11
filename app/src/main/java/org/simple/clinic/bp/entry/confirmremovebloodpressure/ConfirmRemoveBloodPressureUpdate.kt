package org.simple.clinic.bp.entry.confirmremovebloodpressure

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class ConfirmRemoveBloodPressureUpdate : Update<ConfirmRemoveBloodPressureModel, ConfirmRemoveBloodPressureEvent, ConfirmRemoveBloodPressureEffect> {

  override fun update(
      model: ConfirmRemoveBloodPressureModel,
      event: ConfirmRemoveBloodPressureEvent
  ): Next<ConfirmRemoveBloodPressureModel, ConfirmRemoveBloodPressureEffect> {
    return when (event) {
      BloodPressureDeleted -> dispatch(CloseDialog)
      ConfirmRemoveBloodPressureDialogRemoveClicked -> dispatch(DeleteBloodPressure(model.bloodPressureMeasurementUuid))
    }
  }
}
