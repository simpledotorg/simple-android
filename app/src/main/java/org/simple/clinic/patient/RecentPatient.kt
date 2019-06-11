package org.simple.clinic.patient

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import io.reactivex.Flowable
import org.simple.clinic.overdue.Appointment.AppointmentType
import org.simple.clinic.overdue.Appointment.Status
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

data class RecentPatient(

    val uuid: UUID,

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    @Embedded(prefix = "age_")
    val age: Age?,

    val updatedAt: Instant
) {

  @Dao
  interface RoomDao {

    companion object {

      const val RECENT_PATIENT_QUERY = """
        SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.age_computedDateOfBirth,
        MAX(
            IFNULL(P.updatedAt, '0'),
            IFNULL(BP_FOR_ORDERING.latestRecordedAt, '0'),
            IFNULL(PD.latestUpdatedAt, '0'),
            IFNULL(AP.latestUpdatedAt, '0'),
            IFNULL(COMM.latestUpdatedAt, '0'),
            IFNULL(MH.latestUpdatedAt, '0')
        ) updatedAt
        FROM Patient P
          LEFT JOIN (
            SELECT MAX(recordedAt) latestRecordedAt, patientUuid, facilityUuid
              FROM BloodPressureMeasurement
              WHERE facilityUuid = :facilityUuid
              AND deletedAt IS NULL
              GROUP BY patientUuid
          ) BP_FOR_ORDERING ON P.uuid = BP_FOR_ORDERING.patientUuid
          LEFT JOIN (
            SELECT MAX(updatedAt) latestUpdatedAt, patientUuid, facilityUuid
              FROM PrescribedDrug
              WHERE facilityUuid = :facilityUuid
              AND deletedAt IS NULL
              GROUP BY patientUuid
          ) PD ON P.uuid = PD.patientUuid
          LEFT JOIN (
            SELECT MAX(updatedAt) latestUpdatedAt, uuid, patientUuid, facilityUuid
              FROM Appointment
              WHERE facilityUuid = :facilityUuid
              AND deletedAt IS NULL
              AND status = :appointmentStatus
              AND remindOn IS NULL
              AND agreedToVisit IS NULL
              AND appointmentType = :appointmentType
              AND cancelReason IS NULL
              GROUP BY patientUuid
          ) AP ON P.uuid = AP.patientUuid
          LEFT JOIN (
            SELECT MAX(updatedAt) latestUpdatedAt, appointmentUuid
              FROM Communication
              WHERE deletedAt IS NULL
              GROUP BY appointmentUuid
          ) COMM ON AP.uuid = COMM.appointmentUuid
          LEFT JOIN (
            SELECT MAX(updatedAt) latestUpdatedAt, patientUuid
              FROM MedicalHistory
              WHERE deletedAt IS NULL
              GROUP BY patientUuid
          ) MH ON P.uuid = MH.patientUuid
        WHERE (
          BP_FOR_ORDERING.facilityUuid = :facilityUuid OR
          PD.facilityUuid = :facilityUuid OR
          AP.facilityUuid = :facilityUuid
        )
        ORDER BY updatedAt DESC
      """
    }

    /**
    Goal: Fetch a list of patients with 10 most recent changes.
    There are tables like BloodPressureMeasurement (BP), PrescribedDrug (PD), Appointment (AP), etc. Let’s call each table T1, T2, T3, etc.

    Algo:
    1. Get a list of all patients
    2. For each patient, from each table T, get the latest change for them. Columns: T1.latestUpdatedAt, T2.latestUpdatedAt, etc.
    3. Pick latestUpdatedAt for each patient
    4. Order by updatedAt from final list and cap it to 10 entries.
     */
    @Query("$RECENT_PATIENT_QUERY LIMIT :limit")
    fun recentPatients(
        facilityUuid: UUID,
        appointmentStatus: Status,
        appointmentType: AppointmentType,
        limit: Int
    ): Flowable<List<RecentPatient>>

    @Query(RECENT_PATIENT_QUERY)
    fun recentPatients(
        facilityUuid: UUID,
        appointmentStatus: Status,
        appointmentType: AppointmentType
    ): Flowable<List<RecentPatient>>
  }
}
