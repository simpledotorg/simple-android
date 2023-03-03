package org.simple.clinic.home.patients.links

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class PatientsTabLinkModel(
    val facility: Facility?,
) : Parcelable {
  companion object {
    fun default() = PatientsTabLinkModel(
        facility = null,
    )
  }

  val monthlyScreeningReportsEnabled: Boolean
    get() = facility?.config?.monthlyScreeningReportsEnabled == true

  fun currentFacilityLoaded(facility: Facility): PatientsTabLinkModel {
    return copy(facility = facility)
  }
}

