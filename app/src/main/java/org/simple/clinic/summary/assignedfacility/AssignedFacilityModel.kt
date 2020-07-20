package org.simple.clinic.summary.assignedfacility

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import java.util.UUID

@Parcelize
data class AssignedFacilityModel(
    val patientUuid: UUID,
    val assignedFacility: Facility?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID) = AssignedFacilityModel(
        patientUuid = patientUuid,
        assignedFacility = null
    )
  }

  val hasAssignedFacility: Boolean
    get() = assignedFacility != null

  fun assignedFacilityUpdated(facility: Facility?): AssignedFacilityModel {
    return copy(assignedFacility = facility)
  }
}
