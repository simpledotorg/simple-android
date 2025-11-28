package org.simple.clinic.patient

import android.os.Parcelable
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import io.reactivex.Flowable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.overdue.Appointment.AppointmentType
import org.simple.clinic.overdue.Appointment.Status
import org.simple.clinic.util.Unicode
import java.time.Instant
import java.util.UUID
import org.simple.clinic.medicalhistory.Answer as MedicalHistoryAnswer

@Parcelize
data class RecentPatient(

    val uuid: UUID,

    val fullName: String,

    val gender: Gender,

    @Embedded
    val ageDetails: PatientAgeDetails,

    val patientRecordedAt: Instant,

    val updatedAt: Instant,

    val eligibleForReassignment: Answer,

    val diagnosedWithHypertension: MedicalHistoryAnswer?,

    val diagnosedWithDiabetes: MedicalHistoryAnswer?
) : Parcelable {

  override fun toString(): String {
    return "RecentPatient(${Unicode.redacted})"
  }

  @Dao
  interface RoomDao {

    companion object {

      const val RECENT_PATIENT_QUERY = """
        SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value,
               P.age_updatedAt, P.recordedAt patientRecordedAt, P.eligibleForReassignment,
               (
                 SELECT diagnosedWithHypertension
                 FROM MedicalHistory
                 WHERE patientUuid = P.uuid
                 ORDER BY updatedAt DESC
                 LIMIT 1
               ) diagnosedWithHypertension,
               (
                 SELECT hasDiabetes
                 FROM MedicalHistory
                 WHERE patientUuid = P.uuid
                 ORDER BY updatedAt DESC
                 LIMIT 1
               ) diagnosedWithDiabetes,
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
     * Goal: Fetch a list of patients with 10 most recent changes.
     * Algo:
     * 1. Get a list of all patients
     * 2. For each patient, from each table T, get the latest change for them.
     * 3. Pick latestUpdatedAt for each patient
     * 4. Order by updatedAt from final list and cap it to the limit.
     */
    @Query("$RECENT_PATIENT_QUERY LIMIT :limit")
    fun recentPatientsWithLimit(
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
    ): PagingSource<Int, RecentPatient>
  }
}
