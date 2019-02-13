package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.change.FacilityListItem.FacilityOption
import org.simple.clinic.location.Coordinates
import org.simple.clinic.util.Distance
import org.simple.clinic.util.Haversine

object FacilityListItemBuilder {

  fun build(
      facilities: List<Facility>,
      searchQuery: String,
      userLocation: Coordinates?,
      proximityThreshold: Distance
  ): List<FacilityOption> {
    val nearbyFacilities = userLocation
        ?.let { facilitiesNearbyUser(facilities, userLocation, proximityThreshold) }
        ?: emptyList()

    // TODO: Generate section headers
    val nearbyFacilityListItems = nearbyFacilities.map { uiModel(it, searchQuery) }
    val allFacilityListItems = facilities.map { uiModel(it, searchQuery) }

    return nearbyFacilityListItems + allFacilityListItems
  }

  fun build(
      facilities: List<Facility>,
      searchQuery: String
  ): List<FacilityOption> {
    return build(
        facilities = facilities,
        searchQuery = searchQuery,
        userLocation = null,
        proximityThreshold = Distance.ofKilometers(0.0))
  }

  private fun facilitiesNearbyUser(
      facilities: List<Facility>,
      userLocation: Coordinates,
      proximityThreshold: Distance
  ): List<Facility> {
    val facilitiesToDistance = facilities
        .filter { it.location != null }
        .map { it to Haversine.distance(userLocation, it.location!!) }

    val nearbyFacilities = facilitiesToDistance
        .filter { (_, distance) -> distance <= proximityThreshold }

    return nearbyFacilities
        .sortedBy { (_, distance) -> distance }
        .map { (facility, _) -> facility }
  }

  private fun uiModel(facility: Facility, searchQuery: String): FacilityOption {
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

    return FacilityOption(facility = facility, name = highlightedName, address = fullAddress)
  }
}
