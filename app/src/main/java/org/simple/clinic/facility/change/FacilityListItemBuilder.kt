package org.simple.clinic.facility.change

import org.simple.clinic.facility.Facility

object FacilityListItemBuilder {

  fun build(facility: Facility, searchQuery: String): FacilityListItem {
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
      FacilityListItem.Address.WithStreet(street = facility.streetAddress!!, district = facility.district, state = facility.state)
    }

    return FacilityListItem(facility = facility, name = highlightedName, address = fullAddress)
  }
}
