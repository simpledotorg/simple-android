package org.simple.clinic.signature

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SignatureModel : Parcelable {

  companion object {
    fun create() = SignatureModel()
  }
}
