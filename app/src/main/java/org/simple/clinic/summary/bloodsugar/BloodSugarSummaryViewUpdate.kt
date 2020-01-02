package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class BloodSugarSummaryViewUpdate : Update<BloodSugarSummaryViewModel, BloodSugarSummaryViewEvent, BloodSugarSummaryViewEffect> {

  override fun update(
      model: BloodSugarSummaryViewModel,
      event: BloodSugarSummaryViewEvent
  ): Next<BloodSugarSummaryViewModel, BloodSugarSummaryViewEffect> {
    return when (event) {
      is BloodSugarSummaryFetched -> next(model.summaryFetched(event.measurements))
      NewBloodSugarClicked -> dispatch(OpenBloodSugarTypeSelector)
    }
  }
}
