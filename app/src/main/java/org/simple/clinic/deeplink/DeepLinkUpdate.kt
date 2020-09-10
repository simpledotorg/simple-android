package org.simple.clinic.deeplink

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.user.User

class DeepLinkUpdate : Update<DeepLinkModel, DeepLinkEvent, DeepLinkEffect> {

  override fun update(model: DeepLinkModel, event: DeepLinkEvent): Next<DeepLinkModel, DeepLinkEffect> {
    return when (event) {
      is UserFetched -> userFetched(model, event)
      is PatientFetched -> patientFetched(event, model)
    }
  }

  private fun userFetched(
      model: DeepLinkModel,
      event: UserFetched
  ): Next<DeepLinkModel, DeepLinkEffect> {
    return if (event.user?.loggedInStatus == User.LoggedInStatus.LOGGED_IN) {
      checkIfMedicalOfficerHasCapabilitiesToTeleconsult(model, event)
    } else {
      dispatch(NavigateToSetupActivity)
    }
  }

  private fun checkIfMedicalOfficerHasCapabilitiesToTeleconsult(model: DeepLinkModel, event: UserFetched): Next<DeepLinkModel, DeepLinkEffect> {
    val effect = if (event.user?.capabilities?.canTeleconsult == User.CapabilityStatus.Yes) {
      checkIfPatientIsNotNull(model)
    } else {
      ShowTeleconsultLogNotAllowed
    }
    return dispatch(effect)
  }

  private fun checkIfPatientIsNotNull(model: DeepLinkModel): DeepLinkEffect {
    return if (model.patientUuid != null) {
      FetchPatient(model.patientUuid)
    } else {
      ShowNoPatientUuidError
    }
  }

  private fun patientFetched(
      event: PatientFetched,
      model: DeepLinkModel
  ): Next<DeepLinkModel, DeepLinkEffect> {
    val effect = if (event.patient != null) {
      navigateToPatientSummary(model)
    } else {
      ShowPatientDoesNotExist
    }
    return dispatch(effect)
  }

  private fun navigateToPatientSummary(model: DeepLinkModel): DeepLinkEffect {
    return if (model.isLogTeleconsultDeepLink) {
      NavigateToPatientSummaryWithTeleconsultLog(model.patientUuid!!, model.teleconsultRecordId!!)
    } else {
      NavigateToPatientSummary(model.patientUuid!!)
    }
  }
}
