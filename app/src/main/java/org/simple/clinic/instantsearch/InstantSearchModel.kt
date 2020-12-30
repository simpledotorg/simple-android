package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility

data class InstantSearchModel(
    val facility: Facility?,
    val searchQuery: String?
) {

  companion object {
    fun create() = InstantSearchModel(
        facility = null,
        searchQuery = null
    )
  }

  fun facilityLoaded(facility: Facility): InstantSearchModel {
    return copy(facility = facility)
  }
}
