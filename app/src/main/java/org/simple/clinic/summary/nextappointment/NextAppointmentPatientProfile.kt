package org.simple.clinic.summary.nextappointment

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.Patient

@Parcelize
data class NextAppointmentPatientProfile(
    @Embedded
    val appointment: Appointment?,

    @Relation(
        parentColumn = "patientUuid",
        entityColumn = "uuid"
    )
    val patient: Patient,

    @Relation(
        parentColumn = "facilityUuid",
        entityColumn = "uuid"
    )
    val facility: Facility
) : Parcelable
