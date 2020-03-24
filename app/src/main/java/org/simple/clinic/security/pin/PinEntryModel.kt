package org.simple.clinic.security.pin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.security.pin.PinEntryUi.State.PinEntry

@Parcelize
data class PinEntryModel(
    val state: PinEntryUi.State,
    val enteredPin: String,
    val pinDigestToVerify: String?
): Parcelable {

  val hasPinDigestBeenLoaded: Boolean
    get() = pinDigestToVerify != null

  companion object {
    fun default(): PinEntryModel {
      return PinEntryModel(state = PinEntry, enteredPin = "", pinDigestToVerify = null)
    }
  }

  fun enteredPinChanged(pin: String): PinEntryModel {
    return copy(enteredPin = pin)
  }

  fun updatePinDigest(pinDigest: String): PinEntryModel {
    return copy(pinDigestToVerify = pinDigest)
  }
}
