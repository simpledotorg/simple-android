package org.simple.clinic.forgotpin.createnewpin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.User

@Parcelize
data class ForgotPinCreateNewModel(
    val user: User?
) : Parcelable {

  companion object {
    fun create() = ForgotPinCreateNewModel(user = null)
  }

  val hasUser: Boolean
    get() = user != null

  fun userLoaded(user: User): ForgotPinCreateNewModel {
    return copy(user = user)
  }
}
