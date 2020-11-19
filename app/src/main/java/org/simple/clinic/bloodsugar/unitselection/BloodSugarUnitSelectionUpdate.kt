package org.simple.clinic.bloodsugar.unitselection

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class BloodSugarUnitSelectionUpdate : Update<BloodSugarUnitSelectionModel, BloodSugarUnitSelectionEvent, BloodSugarUnitSelectionEffect> {
  override fun update(
      model: BloodSugarUnitSelectionModel,
      event: BloodSugarUnitSelectionEvent
  ): Next<BloodSugarUnitSelectionModel, BloodSugarUnitSelectionEffect> {
    return when (event) {
      BloodSugarUnitSelectionUpdated -> dispatch(CloseDialog)
      is DoneClicked -> dispatch(SaveBloodSugarUnitSelection(event.bloodSugarUnitSelection))
      is SaveBloodSugarUnitPreference -> next(model.bloodSugarUnitPreferenceChanged(event.bloodSugarUnitPreference))
    }
  }
}
