package org.simple.clinic.security.pin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PinEntryModel(
    val enteredPin: String,
    val pinDigestToVerify: String?
): Parcelable {

  companion object {
    fun default(): PinEntryModel {
      return PinEntryModel(enteredPin = "", pinDigestToVerify = null)
    }
  }

  fun enteredPinChanged(pin: String): PinEntryModel {
    return copy(enteredPin = pin)
  }

  fun updatePinDigest(pinDigest: String): PinEntryModel {
    return copy(pinDigestToVerify = pinDigest)
  }
}
