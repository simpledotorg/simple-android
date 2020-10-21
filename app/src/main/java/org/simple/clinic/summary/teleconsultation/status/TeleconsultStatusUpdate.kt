package org.simple.clinic.summary.teleconsultation.status

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class TeleconsultStatusUpdate : Update<TeleconsultStatusModel, TeleconsultStatusEvent, TeleconsultStatusEffect> {

  override fun update(model: TeleconsultStatusModel, event: TeleconsultStatusEvent): Next<TeleconsultStatusModel, TeleconsultStatusEffect> {
    return when (event) {
      is TeleconsultStatusChanged -> next(model.teleconsultStatusChanged(event.teleconsultStatus))
    }
  }
}
