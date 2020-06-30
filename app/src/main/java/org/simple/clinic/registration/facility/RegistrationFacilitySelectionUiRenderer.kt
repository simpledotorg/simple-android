package org.simple.clinic.registration.facility

import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.facility.change.FacilityListItemBuilder
import org.simple.clinic.location.LocationUpdate.Available
import org.simple.clinic.location.LocationUpdate.Unavailable
import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.registration.RegistrationConfig

class RegistrationFacilitySelectionUiRenderer(
    private val ui: RegistrationFacilitySelectionUi,
    private val facilityListItemBuilder: FacilityListItemBuilder,
    private val registrationConfig: RegistrationConfig
) : ViewRenderer<RegistrationFacilitySelectionModel> {

  private var hasShownFacilitiesAtLeastOnce = false

  override fun render(model: RegistrationFacilitySelectionModel) {
    if (model.hasLoadedFacilities) {
      ui.hideProgressIndicator()
      renderFacilities(model)
    } else {
      ui.showProgressIndicator()
    }

    if (model.hasLoadedTotalFacilityCount) {
      if (model.totalFacilityCount!! > 0) {
        ui.showToolbarWithSearchField()
      } else {
        ui.showToolbarWithoutSearchField()
      }
    }
  }

  private fun renderFacilities(model: RegistrationFacilitySelectionModel) {
    val locationCoordinates = when (val locationUpdate = model.currentLocation!!) {
      Unavailable -> null
      is Available -> locationUpdate.location
    }

    val updateType = if (hasShownFacilitiesAtLeastOnce) {
      SUBSEQUENT_UPDATE
    } else {
      hasShownFacilitiesAtLeastOnce = true
      FIRST_UPDATE
    }

    val listItems = facilityListItemBuilder.build(
        facilities = model.facilities!!,
        searchQuery = model.searchQuery,
        userLocation = locationCoordinates,
        proximityThreshold = registrationConfig.proximityThresholdForNearbyFacilities
    )

    ui.updateFacilities(listItems, updateType)
  }
}
