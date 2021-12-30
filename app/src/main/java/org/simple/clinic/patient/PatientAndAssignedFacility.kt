package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility

@Parcelize
data class PatientAndAssignedFacility(
    @Embedded
    val patient: Patient,

    @Relation(
        parentColumn = "assignedFacilityId",
        entityColumn = "uuid"
    )
    val assignedFacility: Facility?
) : Parcelable
