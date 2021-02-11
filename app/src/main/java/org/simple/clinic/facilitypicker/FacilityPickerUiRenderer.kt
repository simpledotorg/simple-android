package org.simple.clinic.facilitypicker

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.mobius.ViewRenderer

class FacilityPickerUiRenderer @AssistedInject constructor(
    private val facilityListItemBuilder: FacilityListItemBuilder,
    private val config: FacilityPickerConfig,
    @Assisted private val ui: FacilityPickerUi
) : ViewRenderer<FacilityPickerModel> {

  @AssistedFactory
  interface Factory {
    fun create(ui: FacilityPickerUi): FacilityPickerUiRenderer
  }

  override fun render(model: FacilityPickerModel) {
    if (model.hasLoadedFacilities) {
      ui.hideProgressIndicator()
      renderFacilities(model)
    } else {
      ui.showProgressIndicator()
    }
  }

  private fun renderFacilities(model: FacilityPickerModel) {
    val locationCoordinates = when (val locationUpdate = model.currentLocation!!) {
      LocationUpdate.Unavailable -> null
      is LocationUpdate.Available -> locationUpdate.location
    }

    val listItems = facilityListItemBuilder.build(
        facilities = model.facilities!!,
        searchQuery = model.searchQuery,
        userLocation = locationCoordinates,
        proximityThreshold = config.proximityThresholdForNearbyFacilities
    )

    ui.updateFacilities(listItems)
  }
}
