package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import java.util.UUID

@Parcelize
data class PatientAndAssignedFacility(
    @Embedded
    val patient: Patient,

    @Relation(
        parentColumn = "assignedFacilityId",
        entityColumn = "uuid"
    )
    val assignedFacility: Facility?
) : Parcelable {

  val hasAssignedFacility: Boolean
    get() = assignedFacility != null

  val assignedFacilityId: UUID
    get() = assignedFacility!!.uuid
}
