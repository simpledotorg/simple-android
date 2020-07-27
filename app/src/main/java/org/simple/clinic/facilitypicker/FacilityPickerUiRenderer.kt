package org.simple.clinic.facilitypicker

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.LocationUpdate
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.registration.RegistrationConfig

class FacilityPickerUiRenderer @AssistedInject constructor(
    private val facilityListItemBuilder: FacilityListItemBuilder,
    private val registrationConfig: RegistrationConfig,
    @Assisted private val ui: FacilityPickerUi
) : ViewRenderer<FacilityPickerModel> {

  @AssistedInject.Factory
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

    if (model.hasLoadedTotalFacilityCount) {
      switchToolbarType(model)
    }
  }

  private fun switchToolbarType(model: FacilityPickerModel) {
    if (model.totalFacilityCount!! > 0) {
      ui.showToolbarWithSearchField()
    } else {
      ui.showToolbarWithoutSearchField()
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
        proximityThreshold = registrationConfig.proximityThresholdForNearbyFacilities
    )

    ui.updateFacilities(listItems)
  }
}
