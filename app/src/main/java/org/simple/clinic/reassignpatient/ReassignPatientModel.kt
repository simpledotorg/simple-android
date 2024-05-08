package org.simple.clinic.reassignpatient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import java.util.UUID

@Parcelize
data class ReassignPatientModel(
    val patientUuid: UUID,
    val assignedFacility: Facility?,
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID) = ReassignPatientModel(
        patientUuid = patientUuid,
        assignedFacility = null,
    )
  }

  val hasAssignedFacility: Boolean
    get() = assignedFacility != null

  fun assignedFacilityUpdated(facility: Facility?): ReassignPatientModel {
    return copy(assignedFacility = facility)
  }
}
