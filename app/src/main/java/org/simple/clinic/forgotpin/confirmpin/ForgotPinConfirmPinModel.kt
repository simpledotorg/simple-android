package org.simple.clinic.forgotpin.confirmpin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.User

@Parcelize
data class ForgotPinConfirmPinModel(
    val user: User?
) : Parcelable {

  companion object {
    fun create() = ForgotPinConfirmPinModel(
        user = null
    )
  }

  val hasUser: Boolean
    get() = user != null

  fun userLoaded(user: User): ForgotPinConfirmPinModel {
    return copy(user = user)
  }
}
