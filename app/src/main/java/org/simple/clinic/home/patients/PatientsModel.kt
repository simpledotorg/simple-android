package org.simple.clinic.home.patients

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.user.User

@Parcelize
data class PatientsModel(
    val user: User?,
    val numberOfPatientsRegistered: Int?
) : Parcelable {

  companion object {

    fun create(): PatientsModel = PatientsModel(
        user = null,
        numberOfPatientsRegistered = null
    )
  }

  val hasLoadedUser: Boolean
    get() = user != null

  fun userLoaded(user: User): PatientsModel {
    return copy(user = user)
  }

  fun numberOfPatientsRegisteredUpdated(numberOfPatientsRegistered: Int): PatientsModel {
    return copy(numberOfPatientsRegistered = numberOfPatientsRegistered)
  }
}
