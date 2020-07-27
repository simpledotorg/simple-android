package org.simple.clinic.forgotpin.createnewpin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ForgotPinCreateNewModel : Parcelable {

  companion object {
    fun create() = ForgotPinCreateNewModel()
  }
}
