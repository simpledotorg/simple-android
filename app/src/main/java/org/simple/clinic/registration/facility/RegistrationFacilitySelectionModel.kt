package org.simple.clinic.registration.facility

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.user.OngoingRegistrationEntry

@Parcelize
data class RegistrationFacilitySelectionModel(
    val ongoingEntry: OngoingRegistrationEntry
) : Parcelable {

  companion object {
    fun create(entry: OngoingRegistrationEntry): RegistrationFacilitySelectionModel {
      return RegistrationFacilitySelectionModel(entry)
    }
  }
}
