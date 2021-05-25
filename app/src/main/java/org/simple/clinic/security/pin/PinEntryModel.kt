package org.simple.clinic.security.pin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PinEntryModel(
    val enteredPin: String
) : Parcelable {

  companion object {
    fun default(): PinEntryModel {
      return PinEntryModel(enteredPin = "")
    }
  }

  fun enteredPinChanged(pin: String): PinEntryModel {
    return copy(enteredPin = pin)
  }
}
