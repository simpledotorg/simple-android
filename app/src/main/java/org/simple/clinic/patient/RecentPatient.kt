package org.simple.clinic.patient

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import io.reactivex.Flowable
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

    @Embedded(prefix = "last_bp_")
    val lastBp: LastBp?
) {

  @Dao
  interface RoomDao {

    companion object {

      const val RECENT_PATIENT_QUERY = """
        SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.age_computedDateOfBirth,
        LAST_BP.systolic last_bp_systolic, LAST_BP.diastolic last_bp_diastolic, LAST_BP.createdAt last_bp_createdAt,
        MAX(
            IFNULL(P.updatedAt, '0'),
            IFNULL(BP_FOR_ORDERING.latestCreatedAt, '0'),
            IFNULL(PD.latestUpdatedAt, '0'),
            IFNULL(AP.latestUpdatedAt, '0'),
            IFNULL(COMM.latestUpdatedAt, '0'),
            IFNULL(MH.latestUpdatedAt, '0')
        ) latestUpdatedAt
        FROM Patient P
          LEFT JOIN (
            SELECT MAX(createdAt) latestCreatedAt, patientUuid, systolic, diastolic, createdAt
              FROM BloodPressureMeasurement
              WHERE deletedAt IS NULL
              GROUP BY patientUuid
          ) LAST_BP ON P.uuid = LAST_BP.patientUuid
          LEFT JOIN (
            SELECT MAX(createdAt) latestCreatedAt, patientUuid, facilityUuid
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
        ORDER BY latestUpdatedAt DESC
      """
    }

    /**
    Goal: Fetch a list of patients with 10 most recent changes.
    There are tables like BloodPressureMeasurement (BP), PrescribedDrug (PD), Appointment (AP), etc. Let’s call each table T1, T2, T3, etc.

    Algo:
    1. Get a list of all patients
    2. For each patient, from each table T, get the latest change for them. Columns: T1.latestUpdatedAt, T2.latestUpdatedAt, etc.
    3. Pick latestUpdatedAt for each patient
    4. Order by latestUpdatedAt from final list and cap it to 10 entries.
     */
    @Query("$RECENT_PATIENT_QUERY LIMIT :limit")
    fun recentPatients(facilityUuid: UUID, limit: Int): Flowable<List<RecentPatient>>

    @Query(RECENT_PATIENT_QUERY)
    fun recentPatients(facilityUuid: UUID): Flowable<List<RecentPatient>>
  }

  data class LastBp(
      val systolic: Int,
      val diastolic: Int,
      val createdAt: Instant
  )
}
