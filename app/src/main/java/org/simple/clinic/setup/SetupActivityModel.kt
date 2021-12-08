package org.simple.clinic.setup

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.util.UtcClock
import java.time.Instant

@Parcelize
data class SetupActivityModel(
    val screenOpenedAt: Instant
) : Parcelable {

  companion object {
    fun create(clock: UtcClock): SetupActivityModel {
      return SetupActivityModel(
          screenOpenedAt = Instant.now(clock)
      )
    }
  }
}
