package org.simple.clinic.bloodsugar.unitselection

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class BloodSugarUnitSelectionUpdate : Update<BloodSugarUnitSelectionModel, BloodSugarUnitSelectionEvent, BloodSugarUnitSelectionEffect> {
  override fun update(
      model: BloodSugarUnitSelectionModel,
      event: BloodSugarUnitSelectionEvent
  ): Next<BloodSugarUnitSelectionModel, BloodSugarUnitSelectionEffect> {
    return when (event) {
      BloodSugarUnitSelectionUpdated -> noChange()
      is DoneClicked -> dispatch(SaveBloodSugarUnitSelection(event.bloodSugarUnitSelection))
    }
  }
}
