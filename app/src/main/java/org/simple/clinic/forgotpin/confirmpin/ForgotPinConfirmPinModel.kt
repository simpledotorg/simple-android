package org.simple.clinic.forgotpin.confirmpin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ForgotPinConfirmPinModel : Parcelable {

  companion object {
    fun create() = ForgotPinConfirmPinModel()
  }
}
