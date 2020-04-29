package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class ConfirmRemoveBloodSugarUpdate : Update<ConfirmRemoveBloodSugarModel, ConfirmRemoveBloodSugarEvent, ConfirmRemoveBloodSugarEffect> {
  override fun update(
      model: ConfirmRemoveBloodSugarModel,
      event: ConfirmRemoveBloodSugarEvent
  ): Next<ConfirmRemoveBloodSugarModel, ConfirmRemoveBloodSugarEffect> {
    return when (event) {
      RemoveBloodSugarClicked -> dispatch(MarkBloodSugarAsDeleted(model.bloodSugarMeasurementUuid))
      BloodSugarMarkedAsDeleted -> dispatch(CloseConfirmRemoveBloodSugarDialog)
    }
  }
}
