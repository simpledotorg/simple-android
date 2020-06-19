package org.simple.clinic.registration.location

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class RegistrationLocationPermissionInit : Init<RegistrationLocationPermissionModel, RegistrationLocationPermissionEffect> {

  override fun init(model: RegistrationLocationPermissionModel): First<RegistrationLocationPermissionModel, RegistrationLocationPermissionEffect> {
    return first(model)
  }
}
