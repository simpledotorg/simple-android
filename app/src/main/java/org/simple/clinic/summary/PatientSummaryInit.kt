package org.simple.clinic.summary

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.teleconsultation.api.TeleconsultInfo

class PatientSummaryInit : Init<PatientSummaryModel, PatientSummaryEffect> {

  override fun init(model: PatientSummaryModel): First<PatientSummaryModel, PatientSummaryEffect> {
    val effects = mutableSetOf<PatientSummaryEffect>(LoadPatientSummaryProfile(model.patientUuid))

    if (!model.hasUserLoggedInStatus || !model.hasLoadedCurrentFacility) {
      effects.add(LoadCurrentUserAndFacility)
    }

    if (model.canCheckTeleconsultationInfo && model.isTeleconsultLogDeepLink.not()) {
      effectForTeleconsultInfoState(model, effects)
    }

    if (!model.hasCheckedForInvalidPhone) {
      effects.add(CheckForInvalidPhone(model.patientUuid))
    }

    if (!model.linkIdWithPatientViewShown && model.openIntention is LinkIdWithPatient) {
      effects.add(ShowLinkIdWithPatientView(model.patientUuid, model.openIntention.identifier))
    }

    return first(model, effects)
  }

  private fun effectForTeleconsultInfoState(
      model: PatientSummaryModel,
      effects: MutableSet<PatientSummaryEffect>
  ) {
    when (model.teleconsultInfo) {
      null, is TeleconsultInfo.Fetching -> {
        effects.add(FetchTeleconsultationInfo(model.currentFacility!!.uuid))
      }
      is TeleconsultInfo.NetworkError -> {
        effects.add(ShowTeleconsultInfoError)
      }
    }
  }
}
