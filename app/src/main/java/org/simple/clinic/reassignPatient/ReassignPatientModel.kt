package org.simple.clinic.reassignPatient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class ReassignPatientModel(
    val assignedFacility: Facility?,
) : Parcelable {

  companion object {
    fun create() = ReassignPatientModel(
        assignedFacility = null,
    )
  }

  fun assignedFacilityUpdated(facility: Facility?): ReassignPatientModel {
    return copy(assignedFacility = facility)
  }
}
