package org.simple.clinic.forgotpin.createnewpin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User

@Parcelize
data class ForgotPinCreateNewModel(
    val user: User?,
    val facility: Facility?,
    val pin: String?
) : Parcelable {

  companion object {
    fun create() = ForgotPinCreateNewModel(user = null, facility = null, pin = null)
  }

  val hasUser: Boolean
    get() = user != null

  val hasFacility: Boolean
    get() = facility != null

  fun userLoaded(user: User): ForgotPinCreateNewModel {
    return copy(user = user)
  }

  fun facilityLoaded(facility: Facility): ForgotPinCreateNewModel {
    return copy(facility = facility)
  }

  fun pinChanged(pin: String): ForgotPinCreateNewModel {
    return copy(pin = pin)
  }
}
