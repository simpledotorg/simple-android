package org.simple.clinic.overdue

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.home.overdue.OverduePatientAddress
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "OverdueAppointment_New")
@Parcelize
data class OverdueAppointment_New(

    @PrimaryKey
    val appointmentId: UUID,

    val patientId: UUID,

    val facilityId: UUID,

    val scheduledDate: LocalDate,

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    @Embedded(prefix = "age_")
    val age: Age?,

    val phoneNumber: String?,

    @Embedded(prefix = "patient_address_")
    val patientAddress: OverduePatientAddress,

    val isAtHighRisk: Boolean,

    val patientLastSeen: Instant,

    val diagnosedWithDiabetes: Answer?,

    val diagnosedWithHypertension: Answer?,

    val patientAssignedFacilityUuid: UUID?,

    val appointmentFacilityName: String?
) : Parcelable {

  val isAppointmentAtAssignedFacility: Boolean
    get() = patientAssignedFacilityUuid == facilityId
}
