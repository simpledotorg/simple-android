package org.simple.clinic.facilitypicker

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init
import org.simple.clinic.facilitypicker.PickFrom.AllFacilities
import org.simple.clinic.facilitypicker.PickFrom.InCurrentGroup
import java.time.Duration

class FacilityPickerInit(
    private val pickFrom: PickFrom,
    private val locationUpdateInterval: Duration,
    private var locationTimeout: Duration,
    private var discardLocationOlderThan: Duration
) : Init<FacilityPickerModel, FacilityPickerEffect> {

  constructor(
      pickFrom: PickFrom,
      config: FacilityPickerConfig
  ) : this(
      pickFrom = pickFrom,
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

    val loadFacilitiesEffect = when (pickFrom) {
      AllFacilities -> LoadFacilitiesWithQuery(model.searchQuery)
      InCurrentGroup -> LoadFacilitiesInCurrentGroup(model.searchQuery)
    }

    effects.add(loadFacilitiesEffect)

    return first(model, effects)
  }
}
