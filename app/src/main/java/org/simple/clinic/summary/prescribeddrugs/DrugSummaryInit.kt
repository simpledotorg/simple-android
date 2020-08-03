package org.simple.clinic.summary.prescribeddrugs

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class DrugSummaryInit : Init<DrugSummaryModel, DrugSummaryEffect> {
  override fun init(model: DrugSummaryModel): First<DrugSummaryModel, DrugSummaryEffect> =
      first(model)
}
