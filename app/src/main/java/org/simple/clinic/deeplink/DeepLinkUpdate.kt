package org.simple.clinic.deeplink

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.user.User

class DeepLinkUpdate : Update<DeepLinkModel, DeepLinkEvent, DeepLinkEffect> {

  override fun update(model: DeepLinkModel, event: DeepLinkEvent): Next<DeepLinkModel, DeepLinkEffect> {
    return when (event) {
      is UserFetched -> {
        val effect = if (event.user == null) {
          NavigateToSetupActivity
        } else {
          if (event.user.loggedInStatus == User.LoggedInStatus.LOGGED_IN && model.patientUuid != null) {
            FetchPatient(model.patientUuid)
          } else {
            NavigateToMainActivity
          }
        }
        dispatch(effect)
      }
      is PatientFetched -> patientFetched(event)
    }
  }

  private fun patientFetched(
      event: PatientFetched
  ): Next<DeepLinkModel, DeepLinkEffect> {
    val patient = event.patient
    return if (patient != null) {
      dispatch(NavigateToPatientSummary(patient.uuid) as DeepLinkEffect)
    } else {
      noChange()
    }
  }
}
