package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility
import org.simple.clinic.location.Coordinates
import org.simple.clinic.util.Distance
import org.simple.clinic.util.Haversine
import org.simple.clinic.util.Kilometers

object FacilityListItemBuilder {

  fun build(
      facilities: List<Facility>,
      searchQuery: String,
      userLocation: Coordinates?,
      proximityThreshold: Distance
  ): List<FacilityListItem> {
    val nearbyFacilities = when {
      userLocation != null -> facilitiesNearbyUser(facilities, userLocation, proximityThreshold)
      else -> emptyList()
    }

    // TODO: Generate section headers
    val nearbyFacilityListItems = nearbyFacilities.map { uiModel(it, searchQuery) }
    val allFacilityListItems = facilities.map { uiModel(it, searchQuery) }

    return nearbyFacilityListItems + allFacilityListItems
  }

  fun build(
      facilities: List<Facility>,
      searchQuery: String
  ): List<FacilityListItem> {
    return build(
        facilities = facilities,
        searchQuery = searchQuery,
        userLocation = null,
        proximityThreshold = Kilometers(0.0))
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
        .sortedBy { (_, distance) -> distance.kilometers() }
        .map { (facility, _) -> facility }
  }

  private fun uiModel(facility: Facility, searchQuery: String): FacilityListItem {
    val canHighlight = searchQuery.isNotBlank() && facility.name.contains(searchQuery, ignoreCase = true)

    val highlightedName = if (canHighlight) {
      FacilityListItem.Name.Highlighted(
          name = facility.name,
          highlightStart = facility.name.indexOf(searchQuery, ignoreCase = true),
          highlightEnd = facility.name.indexOf(searchQuery, ignoreCase = true) + searchQuery.length)
    } else {
      FacilityListItem.Name.Plain(facility.name)
    }

    val fullAddress = if (facility.streetAddress.isNullOrBlank()) {
      FacilityListItem.Address.WithoutStreet(district = facility.district, state = facility.state)
    } else {
      FacilityListItem.Address.WithStreet(street = facility.streetAddress, district = facility.district, state = facility.state)
    }

    return FacilityListItem(facility = facility, name = highlightedName, address = fullAddress)
  }
}
