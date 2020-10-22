package org.simple.clinic.summary.teleconsultation.status

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class TeleconsultStatusUpdate : Update<TeleconsultStatusModel, TeleconsultStatusEvent, TeleconsultStatusEffect> {

  override fun update(model: TeleconsultStatusModel, event: TeleconsultStatusEvent): Next<TeleconsultStatusModel, TeleconsultStatusEffect> {
    return when (event) {
      is TeleconsultStatusChanged -> next(model.teleconsultStatusChanged(event.teleconsultStatus))
      TeleconsultStatusUpdated -> dispatch(CloseSheet)
      DoneClicked -> doneClicked(model)
    }
  }

  private fun doneClicked(model: TeleconsultStatusModel): Next<TeleconsultStatusModel, TeleconsultStatusEffect> {
    return if (model.hasTeleconsultStatus) {
      dispatch(UpdateTeleconsultStatus(model.teleconsultRecordId, model.teleconsultStatus!!))
    } else {
      noChange()
    }
  }
}
