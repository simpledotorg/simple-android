package org.simple.clinic.summary.prescribeddrugs

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class DrugSummaryInit : Init<DrugSummaryModel, DrugSummaryEffect> {
  override fun init(model: DrugSummaryModel): First<DrugSummaryModel, DrugSummaryEffect> {
    return first(model, LoadPrescribedDrugs(model.patientUuid))
  }
}
