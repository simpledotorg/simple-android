package org.simple.clinic.facility.alertchange

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEffect.MarkFacilityChangedAsFalse
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.FacilityChanged
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.FacilityChangedMarkedAsFalse
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.IsFacilityChangedStatusLoaded
import org.simple.clinic.facility.alertchange.AlertFacilityChangeEvent.YesButtonClicked
import org.simple.clinic.facility.alertchange.AlertFacilityChangeViewEffect.CloseSheetWithContinuation
import org.simple.clinic.mobius.dispatch

class AlertFacilityChangeUpdate : Update<AlertFacilityChangeModel, AlertFacilityChangeEvent, AlertFacilityChangeEffect> {

  override fun update(model: AlertFacilityChangeModel, event: AlertFacilityChangeEvent): Next<AlertFacilityChangeModel, AlertFacilityChangeEffect> {
    return when (event) {
      is IsFacilityChangedStatusLoaded -> isFacilityChangeStatusLoaded(event, model)
      FacilityChangedMarkedAsFalse -> dispatch(CloseSheetWithContinuation)
      YesButtonClicked -> dispatch(MarkFacilityChangedAsFalse)
      FacilityChanged -> dispatch(MarkFacilityChangedAsFalse)
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
