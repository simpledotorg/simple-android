package org.simple.clinic.security.pin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant

interface PinEntryUi {

  sealed class Mode : Parcelable {

    @Parcelize
    data object PinEntry : Mode()

    @Parcelize
    data object Progress : Mode()

    @Parcelize
    data class BruteForceLocked(val lockUntil: Instant) : Mode()
  }
}
