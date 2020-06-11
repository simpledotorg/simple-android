package org.simple.clinic.registration.pin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class RegistrationPinModel: Parcelable {

  companion object {
    fun create(): RegistrationPinModel = RegistrationPinModel()
  }
}
