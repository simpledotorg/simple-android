package org.simple.clinic.bp.entry.confirmremovebloodpressure

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class ConfirmRemoveBloodPressureUpdate : Update<ConfirmRemoveBloodPressureModel, ConfirmRemoveBloodPressureEvent, ConfirmRemoveBloodPressureEffect> {

  override fun update(
      model: ConfirmRemoveBloodPressureModel,
      event: ConfirmRemoveBloodPressureEvent
  ): Next<ConfirmRemoveBloodPressureModel, ConfirmRemoveBloodPressureEffect> {
    return noChange()
  }
}
