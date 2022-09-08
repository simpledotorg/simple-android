package org.simple.clinic.home.overdue

import android.os.Parcelable
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import kotlinx.parcelize.Parcelize
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientPhoneNumber
import java.time.LocalDate
import java.util.UUID

@Parcelize
data class OverdueAppointment_Old(

    val fullName: String,

    val gender: Gender,

    @Embedded
    val ageDetails: PatientAgeDetails,

    @Embedded(prefix = "appt_")
    val appointment: Appointment,

    @Embedded(prefix = "phone_")
    val phoneNumber: PatientPhoneNumber?,

    @Embedded(prefix = "patient_address_")
    val patientAddress: OverduePatientAddress,

    val patientAssignedFacilityUuid: UUID?
) : Parcelable {

  @Dao
  interface RoomDao {

    @Query("""
      SELECT 
        P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.assignedFacilityId patientAssignedFacilityUuid,
        
        A.uuid appt_uuid, A.patientUuid appt_patientUuid, A.facilityUuid appt_facilityUuid, A.scheduledDate appt_scheduledDate, A.status appt_status,
        A.cancelReason appt_cancelReason, A.remindOn appt_remindOn, A.agreedToVisit appt_agreedToVisit, A.appointmentType appt_appointmentType,
        A.syncStatus appt_syncStatus, A.createdAt appt_createdAt, A.updatedAt appt_updatedAt, A.deletedAt appt_deletedAt, 
        A.creationFacilityUuid appt_creationFacilityUuid,
        
        PPN.uuid phone_uuid, PPN.patientUuid phone_patientUuid, PPN.number phone_number, PPN.phoneType phone_phoneType, PPN.active phone_active,
        PPN.createdAt phone_createdAt, PPN.updatedAt phone_updatedAt, PPN.deletedAt phone_deletedAt,
        
        PA.streetAddress patient_address_streetAddress, PA.colonyOrVillage patient_address_colonyOrVillage,
        PA.district patient_address_district, PA.state patient_address_state

      FROM Patient P
      
      INNER JOIN (
        SELECT *
        FROM Appointment
        GROUP BY patientUuid HAVING MAX(createdAt)
      ) A ON A.patientUuid = P.uuid
      LEFT JOIN PatientPhoneNumber PPN ON PPN.patientUuid = P.uuid
      LEFT JOIN PatientAddress PA ON PA.uuid = P.addressUuid
      
      WHERE
        IFNULL(patientAssignedFacilityUuid, appt_facilityUuid) = :facilityUuid AND
        (appt_scheduledDate > :scheduledAfter AND appt_scheduledDate < :scheduledBefore) AND
        (appt_remindOn <:scheduledBefore OR appt_remindOn IS NULL) AND
        P.deletedAt IS NULL AND
        P.status != 'dead' AND 
        PPN.deletedAt IS NULL AND 
        A.deletedAt IS NULL AND 
        A.status = 'scheduled'
      GROUP BY appt_patientUuid
      ORDER BY 
        appt_scheduledDate DESC, 
        appt_updatedAt ASC
    """)
    fun overdueInFacilityPagingSource(
        facilityUuid: UUID,
        scheduledBefore: LocalDate,
        scheduledAfter: LocalDate
    ): PagingSource<Int, OverdueAppointment_Old>
  }
}
