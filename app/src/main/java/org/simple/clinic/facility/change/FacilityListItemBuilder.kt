package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption
import org.simple.clinic.location.Coordinates
import org.simple.clinic.location.DistanceCalculator
import org.simple.clinic.util.Distance
import javax.inject.Inject

class FacilityListItemBuilder @Inject constructor(val distanceCalculator: DistanceCalculator) {

  fun build(
      facilities: List<Facility>,
      searchQuery: String,
      userLocation: Coordinates?,
      proximityThreshold: Distance
  ): List<FacilityListItem> {
    val nearbyFacilities = userLocation
        ?.let { facilitiesNearbyUser(facilities, userLocation, proximityThreshold) }
        ?: emptyList()

    val nearbyFacilityItems = nearbyFacilities.mapIndexed { index, facility ->
      uiModel(facility, searchQuery, isLastItemInSection = index == nearbyFacilities.size - 1)
    }

    val allFacilityItems = facilities.mapIndexed { index, facility ->
      uiModel(facility, searchQuery, isLastItemInSection = index == facilities.size - 1)
    }

    val listItems = mutableListOf<FacilityListItem>()
    if (searchQuery.isBlank() && nearbyFacilityItems.isNotEmpty()) {
      listItems.add(FacilityListItem.Header.SuggestedFacilities(hasSpacingWithPreviousSection = false))
      listItems.addAll(nearbyFacilityItems)
      listItems.add(FacilityListItem.Header.AllFacilities(hasSpacingWithPreviousSection = true))
    }
    listItems.addAll(allFacilityItems)
    return listItems
  }

  private fun facilitiesNearbyUser(
      facilities: List<Facility>,
      userLocation: Coordinates,
      proximityThreshold: Distance
  ): List<Facility> {
    val facilitiesToDistance = facilities
        .filter { it.location != null }
        .map { it to distanceCalculator.between(userLocation, it.location!!) }

    val nearbyFacilities = facilitiesToDistance
        .filter { (facility, distance) -> distance <= proximityThreshold }

    return nearbyFacilities
        .sortedBy { (_, distance) -> distance }
        .map { (facility, _) -> facility }
  }

  private fun uiModel(
      facility: Facility,
      searchQuery: String,
      isLastItemInSection: Boolean
  ): FacilityOption {
    val canHighlight = searchQuery.isNotBlank() && facility.name.contains(searchQuery, ignoreCase = true)

    val highlightedName = if (canHighlight) {
      FacilityOption.Name.Highlighted(
          text = facility.name,
          highlightStart = facility.name.indexOf(searchQuery, ignoreCase = true),
          highlightEnd = facility.name.indexOf(searchQuery, ignoreCase = true) + searchQuery.length)
    } else {
      FacilityOption.Name.Plain(facility.name)
    }

    val fullAddress = if (facility.streetAddress.isNullOrBlank()) {
      FacilityOption.Address.WithoutStreet(district = facility.district, state = facility.state)
    } else {
      FacilityOption.Address.WithStreet(street = facility.streetAddress, district = facility.district, state = facility.state)
    }

    return FacilityOption(
        facility = facility,
        name = highlightedName,
        address = fullAddress)
  }
}
