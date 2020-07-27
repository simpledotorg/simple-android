package org.simple.clinic.facilitypicker

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.registration.RegistrationConfig
import java.time.Duration

class FacilityPickerInit(
    private val locationUpdateInterval: Duration,
    private var locationTimeout: Duration,
    private var discardLocationOlderThan: Duration
) : Init<FacilityPickerModel, FacilityPickerEffect> {

  constructor(config: RegistrationConfig) : this(
      locationUpdateInterval = config.locationUpdateInterval,
      locationTimeout = config.locationListenerExpiry,
      discardLocationOlderThan = config.staleLocationThreshold
  )

  override fun init(model: FacilityPickerModel): First<FacilityPickerModel, FacilityPickerEffect> {
    val effects = mutableSetOf<FacilityPickerEffect>()

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
