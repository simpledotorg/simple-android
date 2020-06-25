package org.simple.clinic.registration.facility

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class RegistrationFacilitySelectionModel: Parcelable {

  companion object {
    fun create(): RegistrationFacilitySelectionModel = RegistrationFacilitySelectionModel()
  }
}
