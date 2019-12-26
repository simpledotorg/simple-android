package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.Next
import com.spotify.mobius.Update

class BloodSugarSummaryViewUpdate : Update<BloodSugarSummaryViewModel, BloodSugarSummaryViewEvent, BloodSugarSummaryViewEffect> {

  override fun update(
      model: BloodSugarSummaryViewModel,
      event: BloodSugarSummaryViewEvent
  ): Next<BloodSugarSummaryViewModel, BloodSugarSummaryViewEffect> {
    return Next.next(model.summaryFetched())
  }
}
