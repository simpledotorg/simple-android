package org.simple.clinic.home

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class HomeScreenModel(
    val facility: Facility?
) : Parcelable {

  companion object {
    fun create() = HomeScreenModel(
        facility = null
    )
  }

  val hasFacility: Boolean
    get() = facility != null

  fun facilityLoaded(facility: Facility): HomeScreenModel {
    return copy(facility = facility)
  }
}
