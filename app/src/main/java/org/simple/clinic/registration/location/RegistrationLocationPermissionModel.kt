package org.simple.clinic.registration.location

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationLocationPermissionModel(
    val ongoingRegistrationEntry: OngoingRegistrationEntry
): Parcelable {

  companion object {
    fun create(ongoingRegistrationEntry: OngoingRegistrationEntry): RegistrationLocationPermissionModel {
      return RegistrationLocationPermissionModel(ongoingRegistrationEntry)
    }
  }
}
