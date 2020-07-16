package org.simple.clinic.registration.location

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class RegistrationLocationPermissionUpdate : Update<RegistrationLocationPermissionModel, RegistrationLocationPermissionEvent, RegistrationLocationPermissionEffect> {

  override fun update(
      model: RegistrationLocationPermissionModel,
      event: RegistrationLocationPermissionEvent
  ): Next<RegistrationLocationPermissionModel, RegistrationLocationPermissionEffect> {
    return when (event) {
      is RequestLocationPermission -> openFacilitySelectionScreen(model, event)
      is SkipClicked -> dispatch(OpenFacilitySelectionScreen(model.ongoingRegistrationEntry))
    }
  }

  private fun openFacilitySelectionScreen(
      model: RegistrationLocationPermissionModel,
      event: RequestLocationPermission
  ): Next<RegistrationLocationPermissionModel, RegistrationLocationPermissionEffect> {
    return if (event.isPermissionGranted)
      dispatch(OpenFacilitySelectionScreen(model.ongoingRegistrationEntry) as RegistrationLocationPermissionEffect)
    else
      noChange()
  }
}
