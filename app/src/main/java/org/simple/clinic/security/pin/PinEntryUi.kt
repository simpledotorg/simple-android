package org.simple.clinic.security.pin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Instant

interface PinEntryUi {

  sealed class Mode: Parcelable {

    @Parcelize
    object PinEntry : Mode()

    @Parcelize
    object Progress : Mode()

    @Parcelize
    data class BruteForceLocked(val lockUntil: Instant) : Mode()
  }
}
