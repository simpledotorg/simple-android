package org.simple.clinic.instantsearch

import org.simple.clinic.facility.Facility

data class InstantSearchModel(
    val facility: Facility?
) {

  companion object {
    fun create() = InstantSearchModel(
        facility = null
    )
  }

  fun facilityLoaded(facility: Facility): InstantSearchModel {
    return copy(facility = facility)
  }
}
