package org.simple.clinic.registration.facility

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.registration.RegistrationConfig
import java.time.Duration

class RegistrationFacilitySelectionInit(
    private val locationUpdateInterval: Duration,
    private var locationTimeout: Duration,
    private var discardLocationOlderThan: Duration
) : Init<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEffect> {

  companion object {
    fun create(config: RegistrationConfig): RegistrationFacilitySelectionInit {
      return RegistrationFacilitySelectionInit(
          locationUpdateInterval = config.locationUpdateInterval,
          locationTimeout = config.locationListenerExpiry,
          discardLocationOlderThan = config.staleLocationThreshold
      )
    }
  }

  override fun init(model: RegistrationFacilitySelectionModel): First<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEffect> {
    val effects = mutableSetOf<RegistrationFacilitySelectionEffect>()

    if (!model.hasFetchedLocation) {
      effects.add(FetchCurrentLocation(
          updateInterval = locationUpdateInterval,
          timeout = locationTimeout,
          discardOlderThan = discardLocationOlderThan
      ))
    }

    if (!model.hasLoadedTotalFacilityCount) {
      effects.add(LoadTotalFacilityCount)
    }

    effects.add(LoadFacilitiesWithQuery(model.searchQuery))

    return first(model, effects)
  }
}
