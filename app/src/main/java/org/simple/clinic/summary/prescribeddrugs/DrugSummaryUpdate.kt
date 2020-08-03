package org.simple.clinic.summary.prescribeddrugs

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class DrugSummaryUpdate : Update<DrugSummaryModel, DrugSummaryEvent, DrugSummaryEffect> {
  override fun update(model: DrugSummaryModel, event: DrugSummaryEvent): Next<DrugSummaryModel,
      DrugSummaryEffect> = noChange()
}
