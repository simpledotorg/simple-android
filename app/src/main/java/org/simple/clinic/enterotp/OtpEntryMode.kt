package org.simple.clinic.enterotp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant

sealed class OtpEntryMode : Parcelable {

  @Parcelize
  object OtpEntry : OtpEntryMode()

  @Parcelize
  data class BruteForceOtpEntryLocked(val lockUntil: Instant) : OtpEntryMode()
}
