package org.simple.clinic.signature

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class SignatureModel : Parcelable {

  companion object {
    fun create() = SignatureModel()
  }
}
