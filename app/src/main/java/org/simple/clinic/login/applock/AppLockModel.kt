package org.simple.clinic.login.applock

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User

@Parcelize
data class AppLockModel(
    val user: User?,
    val facility: Facility?
) : Parcelable {

  companion object {
    fun create() = AppLockModel(
        user = null,
        facility = null
    )
  }

  val hasUser: Boolean
    get() = user != null

  val hasFacility: Boolean
    get() = facility != null

  fun userLoaded(user: User): AppLockModel {
    return copy(user = user)
  }

  fun facilityLoaded(facility: Facility): AppLockModel {
    return copy(facility = facility)
  }
}
