package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class MedicalHistorySummaryInit : Init<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {

  override fun init(model: MedicalHistorySummaryModel): First<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    val effects = mutableSetOf<MedicalHistorySummaryEffect>(LoadMedicalHistory(model.patientUuid))

    if (!model.hasLoadedCurrentFacility) {
      effects.add(LoadCurrentFacility)
    }

    return first(model, effects)
  }
}
