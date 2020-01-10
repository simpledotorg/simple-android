package org.simple.clinic.summary

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class PatientSummaryInit : Init<PatientSummaryModel, PatientSummaryEffect> {

  override fun init(model: PatientSummaryModel): First<PatientSummaryModel, PatientSummaryEffect> {
    val effects = if (model.patientSummaryProfile == null) {
      setOf(LoadPatientSummaryProfile(model.patientUuid))
    } else {
      emptySet()
    }

    return first(model, effects)
  }
}
