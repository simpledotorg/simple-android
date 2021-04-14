package org.simple.clinic.home.patients

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.user.User

@Parcelize
data class PatientsTabModel(
    val user: User?,
    val numberOfPatientsRegistered: Int?
) : Parcelable {

  companion object {

    fun create(): PatientsTabModel = PatientsTabModel(
        user = null,
        numberOfPatientsRegistered = null
    )
  }

  val hasLoadedUser: Boolean
    get() = user != null

  val hasLoadedNumberOfPatientsRegistered: Boolean
    get() = numberOfPatientsRegistered != null

  fun userLoaded(user: User): PatientsTabModel {
    return copy(user = user)
  }

  fun numberOfPatientsRegisteredUpdated(numberOfPatientsRegistered: Int): PatientsTabModel {
    return copy(numberOfPatientsRegistered = numberOfPatientsRegistered)
  }
}
