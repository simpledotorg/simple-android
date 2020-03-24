package org.simple.clinic.security.pin

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.Instant

interface PinEntryUi: UiActions {

  // Not yet migrated to Mobius
  fun setForgotButtonVisible(visible: Boolean)

  sealed class State: Parcelable {

    @Parcelize
    object PinEntry : State()

    @Parcelize
    object Progress : State()

    @Parcelize
    data class BruteForceLocked(val lockUntil: Instant) : State()
  }
}
