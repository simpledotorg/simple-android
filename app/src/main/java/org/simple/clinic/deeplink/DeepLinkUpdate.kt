package org.simple.clinic.deeplink

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.user.User

class DeepLinkUpdate : Update<DeepLinkModel, DeepLinkEvent, DeepLinkEffect> {

  override fun update(model: DeepLinkModel, event: DeepLinkEvent): Next<DeepLinkModel, DeepLinkEffect> {
    return when (event) {
      is UserFetched -> userFetched(model, event)
      is PatientFetched -> patientFetched(event)
    }
  }

  private fun userFetched(
      model: DeepLinkModel,
      event: UserFetched
  ): Next<DeepLinkModel, DeepLinkEffect> {
    return when {
      event.user?.loggedInStatus == User.LoggedInStatus.LOGGED_IN -> {
        val effect = if (model.patientUuid != null) {
          FetchPatient(model.patientUuid)
        } else {
          NavigateToMainActivity
        }
        dispatch(effect)
      }
      else -> {
        dispatch(NavigateToSetupActivity)
      }
    }
  }

  private fun patientFetched(
      event: PatientFetched
  ): Next<DeepLinkModel, DeepLinkEffect> {
    val patient = event.patient
    val effect = if (patient != null) {
      NavigateToPatientSummary(patient.uuid)
    } else {
      ShowPatientDoesNotExist
    }
    return dispatch(effect)
  }
}
