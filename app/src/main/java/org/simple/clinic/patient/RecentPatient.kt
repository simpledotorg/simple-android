package org.simple.clinic.patient

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import io.reactivex.Flowable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.overdue.Appointment.AppointmentType
import org.simple.clinic.overdue.Appointment.Status
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Parcelize
data class RecentPatient(

    val uuid: UUID,

    val fullName: String,

    val gender: Gender,

    val dateOfBirth: LocalDate?,

    @Embedded(prefix = "age_")
    val age: Age?,

    val patientRecordedAt: Instant,

    val updatedAt: Instant
): Parcelable {

  @Dao
  interface RoomDao {

    companion object {

      const val RECENT_PATIENT_QUERY = """
        SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.recordedAt patientRecordedAt,
        MAX(
            IFNULL(BP.latestRecordedAt, '0'),
            IFNULL(PD.latestUpdatedAt, '0'),
            IFNULL(AP.latestCreatedAt, '0'),
            IFNULL(BloodSugar.latestRecordedAt, '0')
        ) updatedAt
        FROM Patient P
          LEFT JOIN (
            SELECT MAX(recordedAt) latestRecordedAt, patientUuid, facilityUuid
              FROM BloodPressureMeasurement
              WHERE facilityUuid = :facilityUuid
              AND deletedAt IS NULL
              GROUP BY patientUuid
          ) BP ON P.uuid = BP.patientUuid
          LEFT JOIN (
            SELECT MAX(updatedAt) latestUpdatedAt, patientUuid, facilityUuid
              FROM PrescribedDrug
              WHERE facilityUuid = :facilityUuid
              AND deletedAt IS NULL
              GROUP BY patientUuid
          ) PD ON P.uuid = PD.patientUuid
          LEFT JOIN (
            SELECT MAX(createdAt) latestCreatedAt, uuid, patientUuid, facilityUuid, creationFacilityUuid
              FROM Appointment
              WHERE creationFacilityUuid = :facilityUuid
              AND deletedAt IS NULL
              AND status = :appointmentStatus
              AND appointmentType = :appointmentType
              GROUP BY patientUuid
          ) AP ON P.uuid = AP.patientUuid
          LEFT JOIN (
            SELECT MAX(recordedAt) latestRecordedAt, patientUuid, facilityUuid
              FROM BloodSugarMeasurements
              WHERE facilityUuid = :facilityUuid
              AND deletedAt IS NULL
              GROUP BY patientUuid
          ) BloodSugar ON P.uuid = BloodSugar.patientUuid
        WHERE (
          (
            BP.facilityUuid = :facilityUuid OR
            PD.facilityUuid = :facilityUuid OR
            AP.creationFacilityUuid = :facilityUuid OR
            BloodSugar.facilityUuid = :facilityUuid
          ) 
          AND P.deletedAt IS NULL
          AND P.status = :patientStatus
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
        patientStatus: PatientStatus,
        limit: Int
    ): Flowable<List<RecentPatient>>

    @Query(RECENT_PATIENT_QUERY)
    fun recentPatients(
        facilityUuid: UUID,
        appointmentStatus: Status,
        appointmentType: AppointmentType,
        patientStatus: PatientStatus
    ): Flowable<List<RecentPatient>>
  }
}
