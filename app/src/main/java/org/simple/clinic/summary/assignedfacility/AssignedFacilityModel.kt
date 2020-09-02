package org.simple.clinic.summary.assignedfacility

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.summary.PatientSummaryChildModel
import java.util.UUID

@Parcelize
data class AssignedFacilityModel(
    val patientUuid: UUID,
    val assignedFacility: Facility?
) : Parcelable, PatientSummaryChildModel {

  companion object {
    fun create(patientUuid: UUID) = AssignedFacilityModel(
        patientUuid = patientUuid,
        assignedFacility = null
    )
  }

  val hasAssignedFacility: Boolean
    get() = assignedFacility != null

  override fun readyToRender(): Boolean {
    // We don't need to care about this since the parent view
    // hides this view if there is no assigned facility.
    return true
  }

  fun assignedFacilityUpdated(facility: Facility?): AssignedFacilityModel {
    return copy(assignedFacility = facility)
  }
}
