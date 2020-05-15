package org.simple.clinic.summary

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.teleconsultation.api.TeleconsultInfo

class PatientSummaryInit : Init<PatientSummaryModel, PatientSummaryEffect> {

  override fun init(model: PatientSummaryModel): First<PatientSummaryModel, PatientSummaryEffect> {
    val effects = mutableSetOf<PatientSummaryEffect>(LoadPatientSummaryProfile(model.patientUuid))

    if (!model.hasUserLoggedInStatus) {
      effects.add(LoadUserLoggedInStatus)
    }

    if (!model.hasLoadedCurrentFacility) {
      effects.add(LoadCurrentFacility)
    } else {
      when (model.teleconsultInfo) {
        null, is TeleconsultInfo.Fetching -> {
          effects.add(FetchTeleconsultationInfo(model.currentFacility!!.uuid))
        }
        is TeleconsultInfo.NetworkError -> {
          effects.add(ShowTeleconsultInfoError)
        }
      }
    }

    if (!model.hasCheckedForInvalidPhone) {
      effects.add(CheckForInvalidPhone(model.patientUuid))
    }

    if (!model.linkIdWithPatientViewShown && model.openIntention is LinkIdWithPatient) {
      effects.add(ShowLinkIdWithPatientView(model.patientUuid, model.openIntention.identifier))
    }

    return first(model, effects)
  }
}
