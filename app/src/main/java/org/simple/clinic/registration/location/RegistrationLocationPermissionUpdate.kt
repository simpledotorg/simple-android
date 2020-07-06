package org.simple.clinic.registration.location

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class RegistrationLocationPermissionUpdate : Update<RegistrationLocationPermissionModel,
    RegistrationLocationPermissionEvent, RegistrationLocationPermissionEffect> {

  override fun update(
      model: RegistrationLocationPermissionModel,
      event: RegistrationLocationPermissionEvent
  ): Next<RegistrationLocationPermissionModel, RegistrationLocationPermissionEffect> {
    when (event) {
      is RequestLocationPermission -> {
        if (event.isPermissionGranted) {
          return dispatch(OpenFacilitySelectionScreen)
        }

        return noChange()
      }
    }
  }
}
