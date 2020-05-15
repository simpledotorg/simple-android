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
      is PatientFetched -> noChange()
    }
  }
}
