package org.simple.clinic.facility.alertchange

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.FacilityChangedMarkedAsFalse
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.IsFacilityChangedStatusLoaded
import org.simple.clinic.facility.alertchange.AlertFacilityChangeViewEffect.CloseSheetWithContinuation

class AlertFacilityChangeUpdate : Update<AlertFacilityChangeModel, AlertFacilityChangeEvent, AlertFacilityChangeEffect> {

  override fun update(model: AlertFacilityChangeModel, event: AlertFacilityChangeEvent): Next<AlertFacilityChangeModel, AlertFacilityChangeEffect> {
    return when (event) {
      is IsFacilityChangedStatusLoaded -> isFacilityChangeStatusLoaded(event, model)
      FacilityChangedMarkedAsFalse -> noChange()
    }
  }

  private fun isFacilityChangeStatusLoaded(
      event: IsFacilityChangedStatusLoaded,
      model: AlertFacilityChangeModel
  ): Next<AlertFacilityChangeModel, AlertFacilityChangeEffect> {
    val effects: Set<AlertFacilityChangeEffect> = if (event.isFacilityChanged) {
      emptySet()
    } else {
      setOf(CloseSheetWithContinuation)
    }

    return next(
        model.updateIsFacilityChanged(isFacilityChanged = event.isFacilityChanged),
        effects
    )
  }
}
