package org.simple.clinic.registration.location

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class RegistrationLocationPermissionModel: Parcelable {

  companion object {
    fun create(): RegistrationLocationPermissionModel = RegistrationLocationPermissionModel()
  }
}
