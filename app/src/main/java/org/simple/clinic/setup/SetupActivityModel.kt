package org.simple.clinic.setup

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.appconfig.Country
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.isNotEmpty
import java.time.Instant
import java.util.Optional

@Parcelize
data class SetupActivityModel(
    val hasUserSelectedACountry: Boolean?,
    val screenOpenedAt: Instant
) : Parcelable {

  companion object {
    fun create(clock: UtcClock): SetupActivityModel {
      return SetupActivityModel(
          hasUserSelectedACountry = null,
          screenOpenedAt = Instant.now(clock)
      )
    }
  }

  fun withSelectedCountry(country: Optional<Country>): SetupActivityModel {
    return copy(hasUserSelectedACountry = country.isNotEmpty())
  }
}
