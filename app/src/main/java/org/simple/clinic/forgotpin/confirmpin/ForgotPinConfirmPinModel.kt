package org.simple.clinic.forgotpin.confirmpin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User

@Parcelize
data class ForgotPinConfirmPinModel(
    val user: User?,
    val facility: Facility?,
    val previousPin: String
) : Parcelable {

  companion object {
    fun create(previousPin: String) = ForgotPinConfirmPinModel(
        user = null,
        facility = null,
        previousPin = previousPin
    )
  }

  val hasUser: Boolean
    get() = user != null

  val hasFacility: Boolean
    get() = facility != null

  fun userLoaded(user: User): ForgotPinConfirmPinModel {
    return copy(user = user)
  }

  fun facilityLoaded(facility: Facility): ForgotPinConfirmPinModel {
    return copy(facility = facility)
  }
}
