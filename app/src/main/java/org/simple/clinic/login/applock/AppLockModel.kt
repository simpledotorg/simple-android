package org.simple.clinic.login.applock

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.User

@Parcelize
data class AppLockModel(
    val user: User?
) : Parcelable {

  companion object {
    fun create() = AppLockModel(
        user = null
    )
  }

  val hasUser: Boolean
    get() = user != null

  fun userLoaded(user: User): AppLockModel {
    return copy(user = user)
  }
}
