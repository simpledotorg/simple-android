package org.simple.clinic.summary

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class PatientSummaryInit : Init<PatientSummaryModel, PatientSummaryEffect> {

  override fun init(model: PatientSummaryModel): First<PatientSummaryModel, PatientSummaryEffect> {
    val effects = mutableSetOf<PatientSummaryEffect>(LoadPatientSummaryProfile(model.patientUuid))

    if (!model.hasUserLoggedInStatus || !model.hasLoadedCurrentFacility) {
      effects.add(LoadCurrentUserAndFacility)
    }

    if (!model.hasCheckedForInvalidPhone) {
      effects.add(CheckForInvalidPhone(model.patientUuid))
    }

    if (model.hasMedicalOfficers.not()) {
      effects.add(LoadMedicalOfficers)
    }

    return first(model, effects)
  }
}
