package org.simple.clinic.home.patients

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.User

@Parcelize
data class PatientsModel(
    val user: User?
) : Parcelable {

  companion object {

    fun create(): PatientsModel = PatientsModel(
        user = null
    )
  }

  val hasLoadedUser: Boolean
    get() = user != null

  fun userLoaded(user: User): PatientsModel {
    return copy(user = user)
  }
}
