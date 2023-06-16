package org.simple.clinic.home.overdue

import android.os.Parcelable
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Observable
import kotlinx.parcelize.Parcelize
import org.intellij.lang.annotations.Language
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.callresult.CallResult
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientPhoneNumber
import java.time.LocalDate
import java.util.UUID

@Parcelize
data class OverdueAppointment(

    val fullName: String,

    val gender: Gender,

    @Embedded
    val ageDetails: PatientAgeDetails,

    @Embedded(prefix = "phone_")
    val phoneNumber: PatientPhoneNumber?,

    @Embedded(prefix = "patient_address_")
    val patientAddress: OverduePatientAddress,

    @Embedded(prefix = "appt_")
    val appointment: Appointment,

    @Embedded(prefix = "call_result_")
    val callResult: CallResult?,

    val patientAssignedFacilityUuid: UUID?
) : Parcelable {

  @Dao
  interface RoomDao {

    companion object {

      @Language("RoomSql")
      private const val OVERDUE_APPOINTMENTS_QUERY = """
      SELECT 
        P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.assignedFacilityId patientAssignedFacilityUuid,
        
        A.uuid appt_uuid, A.patientUuid appt_patientUuid, A.facilityUuid appt_facilityUuid, A.scheduledDate appt_scheduledDate, A.status appt_status,
        A.cancelReason appt_cancelReason, A.remindOn appt_remindOn, A.agreedToVisit appt_agreedToVisit, A.appointmentType appt_appointmentType,
        A.syncStatus appt_syncStatus, A.createdAt appt_createdAt, A.updatedAt appt_updatedAt, A.deletedAt appt_deletedAt, 
        A.creationFacilityUuid appt_creationFacilityUuid,
        
        PPN.uuid phone_uuid, PPN.patientUuid phone_patientUuid, PPN.number phone_number, PPN.phoneType phone_phoneType, PPN.active phone_active,
        PPN.createdAt phone_createdAt, PPN.updatedAt phone_updatedAt, PPN.deletedAt phone_deletedAt,
        
        PA.streetAddress patient_address_streetAddress, PA.colonyOrVillage patient_address_colonyOrVillage,
        PA.district patient_address_district, PA.state patient_address_state,
        
        CR.id call_result_id, CR.userId call_result_userId, CR.appointmentId call_result_appointmentId,
        CR.removeReason call_result_removeReason, CR.outcome call_result_outcome, CR.createdAt call_result_createdAt,
        CR.updatedAt call_result_updatedAt, CR.deletedAt call_result_deletedAt, CR.syncStatus call_result_syncStatus

      FROM (
        SELECT * FROM Patient
        WHERE deletedAt IS NULL AND status != 'dead'
      ) P
      
      INNER JOIN (
        SELECT * FROM Appointment 
        WHERE 
          (patientUuid IN ( SELECT uuid FROM Patient WHERE assignedFacilityId = :facilityUuid ) OR facilityUuid = :facilityUuid) AND
          deletedAt IS NULL
        GROUP BY patientUuid HAVING MAX(createdAt)
      ) A ON A.patientUuid = P.uuid

      LEFT JOIN PatientPhoneNumber PPN ON PPN.patientUuid = P.uuid

      LEFT JOIN PatientAddress PA ON PA.uuid = P.addressUuid
      
      LEFT JOIN (
        SELECT * FROM CallResult
        GROUP BY appointmentId HAVING MAX(createdAt)
      ) CR ON CR.appointmentId = A.uuid
    """
    }

    @Transaction
    @Query(""" 
      $OVERDUE_APPOINTMENTS_QUERY 
      WHERE
        IFNULL(patientAssignedFacilityUuid, appt_facilityUuid) = :facilityUuid AND
        appt_scheduledDate < :scheduledBefore AND
        appt_status != "visited"
      GROUP BY appt_patientUuid
      ORDER BY 
        appt_scheduledDate DESC, 
        appt_updatedAt ASC
    """)
    fun overdueAppointmentsInFacility(
        facilityUuid: UUID,
        scheduledBefore: LocalDate
    ): Observable<List<OverdueAppointment>>

    @Transaction
    @Query("""
      $OVERDUE_APPOINTMENTS_QUERY 
      WHERE
        IFNULL(patientAssignedFacilityUuid, appt_facilityUuid) = :facilityUuid AND
        appt_scheduledDate < date(:scheduledBefore, '-1 year') AND
        appt_status != "visited"
      GROUP BY appt_patientUuid
      ORDER BY 
        appt_scheduledDate DESC, 
        appt_updatedAt ASC
    """)
    fun moreThanAnYearOverdueAppointments(facilityUuid: UUID, scheduledBefore: LocalDate): Observable<List<OverdueAppointment>>

    @Transaction
    @Query("""
      $OVERDUE_APPOINTMENTS_QUERY 
      WHERE
        IFNULL(patientAssignedFacilityUuid, appt_facilityUuid) = :facilityUuid AND
        appt_scheduledDate > date(:scheduledBefore, '-1 year') AND
        appt_status == "cancelled" AND
        call_result_outcome == "removed_from_overdue_list"
      GROUP BY appt_patientUuid
      ORDER BY 
        appt_scheduledDate DESC, 
        appt_updatedAt ASC
    """)
    fun removedOverdueAppointments(facilityUuid: UUID, scheduledBefore: LocalDate): Observable<List<OverdueAppointment>>

    @Transaction
    @Query("""
      $OVERDUE_APPOINTMENTS_QUERY 
      WHERE
        IFNULL(patientAssignedFacilityUuid, appt_facilityUuid) = :facilityUuid AND
        appt_scheduledDate > date(:scheduledBefore, '-1 year') AND
        appt_status != "visited" AND
        call_result_outcome == "remind_to_call_later"
      GROUP BY appt_patientUuid
      ORDER BY 
        appt_scheduledDate DESC, 
        appt_updatedAt ASC
    """)
    fun remindToCallLaterOverdueAppointments(facilityUuid: UUID, scheduledBefore: LocalDate): Observable<List<OverdueAppointment>>

    @Transaction
    @Query("""
      $OVERDUE_APPOINTMENTS_QUERY 
      WHERE
        IFNULL(patientAssignedFacilityUuid, appt_facilityUuid) = :facilityUuid AND
        appt_scheduledDate > date(:scheduledBefore, '-1 year') AND
        appt_status != "visited" AND
        call_result_outcome == "agreed_to_visit"
      GROUP BY appt_patientUuid
      ORDER BY 
        appt_scheduledDate DESC, 
        appt_updatedAt ASC
    """)
    fun agreedToVisitOverdueAppointments(facilityUuid: UUID, scheduledBefore: LocalDate): Observable<List<OverdueAppointment>>

    @Transaction
    @Query("""
      $OVERDUE_APPOINTMENTS_QUERY 
      WHERE
        IFNULL(patientAssignedFacilityUuid, appt_facilityUuid) = :facilityUuid AND
        appt_scheduledDate > date(:scheduledBefore, '-1 year') AND
        appt_status != "visited" AND
        call_result_outcome IS NULL
      GROUP BY appt_patientUuid
      ORDER BY 
        appt_scheduledDate DESC, 
        appt_updatedAt ASC
    """)
    fun pendingOverdueAppointments(facilityUuid: UUID, scheduledBefore: LocalDate): Observable<List<OverdueAppointment>>

    @Transaction
    @Query("""
      $OVERDUE_APPOINTMENTS_QUERY
      WHERE
        (
          P.uuid IN (SELECT uuid FROM PatientFts WHERE fullName MATCH :query) OR
          P.addressUuid IN (SELECT uuid FROM PatientAddressFts WHERE colonyOrVillage MATCH :query)
        ) AND
        IFNULL(patientAssignedFacilityUuid, appt_facilityUuid) = :facilityUuid AND
        appt_scheduledDate < :scheduledBefore AND
        appt_status != "visited"
      GROUP BY appt_patientUuid
      ORDER BY fullName COLLATE NOCASE
    """)
    fun search(
        query: String,
        facilityUuid: UUID,
        scheduledBefore: LocalDate
    ): PagingSource<Int, OverdueAppointment>

    @Transaction
    @Query("""
      $OVERDUE_APPOINTMENTS_QUERY
      WHERE
        (
          P.uuid IN (SELECT uuid FROM PatientFts WHERE fullName MATCH :query) OR
          P.addressUuid IN (SELECT uuid FROM PatientAddressFts WHERE colonyOrVillage MATCH :query)
        ) AND
        IFNULL(patientAssignedFacilityUuid, appt_facilityUuid) = :facilityUuid AND
        appt_scheduledDate < :scheduledBefore AND
        appt_status != "visited"
      GROUP BY appt_patientUuid
      ORDER BY fullName COLLATE NOCASE
    """)
    fun searchImmediate(
        query: String,
        facilityUuid: UUID,
        scheduledBefore: LocalDate
    ): List<OverdueAppointment>
  }
}
