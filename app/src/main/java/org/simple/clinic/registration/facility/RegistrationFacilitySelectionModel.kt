package org.simple.clinic.registration.facility

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
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

  fun withUpdatedEntry(updatedEntry: OngoingRegistrationEntry): RegistrationFacilitySelectionModel {
    return copy(ongoingEntry = updatedEntry)
  }
}
