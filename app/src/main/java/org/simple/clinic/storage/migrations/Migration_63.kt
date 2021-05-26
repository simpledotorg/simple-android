package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_63 @Inject constructor() : Migration(62, 63) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        CREATE VIEW `OverdueAppointment` AS SELECT P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt,

          A.uuid appt_uuid, A.patientUuid appt_patientUuid, A.facilityUuid appt_facilityUuid, A.scheduledDate appt_scheduledDate, A.status appt_status,
          A.cancelReason appt_cancelReason, A.remindOn appt_remindOn, A.agreedToVisit appt_agreedToVisit, A.appointmentType appt_appointmentType,
          A.syncStatus appt_syncStatus, A.createdAt appt_createdAt, A.updatedAt appt_updatedAt, A.creationFacilityUuid appt_creationFacilityUuid,

          PPN.uuid phone_uuid, PPN.patientUuid phone_patientUuid, PPN.number phone_number, PPN.phoneType phone_phoneType, PPN.active phone_active,
          PPN.createdAt phone_createdAt, PPN.updatedAt phone_updatedAt,

          MH.hasDiabetes diagnosedWithDiabetes, MH.diagnosedWithHypertension diagnosedWithHypertension,

          (
            CASE
                WHEN BP.uuid IS NULL THEN BloodSugar.recordedAt
                WHEN BloodSugar.uuid IS NULL THEN BP.recordedAt
                ELSE MAX(BP.recordedAt, BloodSugar.recordedAt)
            END
          ) AS patientLastSeen,

          (
            CASE
              WHEN BloodSugar.reading_type = 'fasting' AND CAST (BloodSugar.reading_value AS REAL) >= 200 THEN 1
              WHEN BloodSugar.reading_type = 'random' AND CAST (BloodSugar.reading_value AS REAL) >= 300 THEN 1
              WHEN BloodSugar.reading_type = 'post_prandial' AND CAST (BloodSugar.reading_value AS REAL) >= 300 THEN 1
              WHEN BloodSugar.reading_type = 'hba1c' AND CAST (BloodSugar.reading_value AS REAL) >= 9 THEN 1
              WHEN BP.systolic >= 180 OR BP.diastolic >= 110 THEN 1
              WHEN (MH.hasHadHeartAttack = 'yes' OR MH.hasHadStroke = 'yes') AND (BP.systolic >= 140 OR BP.diastolic >= 110) 
                THEN 1 
              ELSE 0
            END
          ) AS isAtHighRisk

          FROM Patient P

          INNER JOIN Appointment A ON A.patientUuid = P.uuid
          LEFT JOIN PatientPhoneNumber PPN ON (PPN.patientUuid = P.uuid AND PPN.deletedAt IS NULL)
          LEFT JOIN MedicalHistory MH ON MH.patientUuid = P.uuid

          LEFT JOIN (
            SELECT * FROM BloodPressureMeasurement WHERE deletedAt IS NULL GROUP BY patientUuid HAVING max(recordedAt)
          ) BP ON BP.patientUuid = P.uuid

          LEFT JOIN (
            SELECT * FROM BloodSugarMeasurements WHERE deletedAt IS NULL GROUP BY patientUuid HAVING max(recordedAt)
          ) BloodSugar ON BloodSugar.patientUuid = P.uuid
          
          WHERE 
            P.deletedAt IS NULL
            AND A.deletedAt IS NULL
            AND A.status = 'scheduled'
            AND PPN.number IS NOT NULL
            AND (BP.recordedAt IS NOT NULL OR BloodSugar.recordedAt IS NOT NULL)
      """)
    }
  }
}
