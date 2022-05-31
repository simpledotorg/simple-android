package org.simple.clinic.home.overdue

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class OverdueModel(
    val facility: Facility?
) : Parcelable {

  companion object {
    fun create(): OverdueModel {
      return OverdueModel(
          facility = null
      )
    }
  }

  val isDiabetesManagementEnabled: Boolean
    get() = facility!!.config.diabetesManagementEnabled

  val hasLoadedCurrentFacility: Boolean
    get() = facility != null

  fun currentFacilityLoaded(facility: Facility): OverdueModel {
    return copy(facility = facility)
  }
}
