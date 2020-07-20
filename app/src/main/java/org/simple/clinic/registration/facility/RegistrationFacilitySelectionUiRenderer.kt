package org.simple.clinic.registration.facility

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.LocationUpdate.Available
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.registration.RegistrationConfig

class RegistrationFacilitySelectionUiRenderer @AssistedInject constructor(
    @Assisted private val ui: RegistrationFacilitySelectionUi,
    private val facilityListItemBuilder: FacilityListItemBuilder,
    private val registrationConfig: RegistrationConfig
) : ViewRenderer<RegistrationFacilitySelectionModel> {

  @AssistedInject.Factory
  interface Factory {
    fun create(ui: RegistrationFacilitySelectionUi): RegistrationFacilitySelectionUiRenderer
  }

  override fun render(model: RegistrationFacilitySelectionModel) {
    if (model.hasLoadedFacilities) {
      ui.hideProgressIndicator()
      renderFacilities(model)
    } else {
      ui.showProgressIndicator()
    }

    if (model.hasLoadedTotalFacilityCount) {
      toggleSearchFieldInToolbar(model)
    }
  }

  private fun toggleSearchFieldInToolbar(model: RegistrationFacilitySelectionModel) {
    if (model.totalFacilityCount!! > 0) {
      ui.showToolbarWithSearchField()
    } else {
      ui.showToolbarWithoutSearchField()
    }
  }

  private fun renderFacilities(model: RegistrationFacilitySelectionModel) {
    val locationCoordinates = when (val locationUpdate = model.currentLocation!!) {
      Unavailable -> null
      is Available -> locationUpdate.location
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
