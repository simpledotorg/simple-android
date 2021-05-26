package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_81 @Inject constructor() : Migration(80, 81) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("PRAGMA foreign_keys = OFF")

    database.inTransaction {
      execSQL(""" ALTER TABLE "Facility" RENAME TO "Facility_OLD" """)
      execSQL(""" CREATE TABLE IF NOT EXISTS "Facility" (
        "uuid" TEXT NOT NULL, 
        "name" TEXT NOT NULL, 
        "facilityType" TEXT, 
        "streetAddress" TEXT, 
        "villageOrColony" TEXT, 
        "district" TEXT NOT NULL, 
        "state" TEXT NOT NULL, 
        "country" TEXT NOT NULL, 
        "pinCode" TEXT, 
        "protocolUuid" TEXT, 
        "groupUuid" TEXT, 
        "createdAt" TEXT NOT NULL, 
        "updatedAt" TEXT NOT NULL, 
        "syncStatus" TEXT NOT NULL, 
        "deletedAt" TEXT, 
        "syncGroup" TEXT NOT NULL, 
        "location_latitude" REAL, 
        "location_longitude" REAL, 
        "config_diabetesManagementEnabled" INTEGER NOT NULL, 
        "config_teleconsultationEnabled" INTEGER, 
        PRIMARY KEY("uuid")) """
      )
      execSQL(""" INSERT INTO "Facility" (
            "uuid",
            "name",
            "facilityType",
            "streetAddress",
            "villageOrColony",
            "district",
            "state",
            "country",
            "pinCode",
            "protocolUuid",
            "groupUuid",
            "createdAt",
            "updatedAt",
            "syncStatus",
            "deletedAt",
            "syncGroup",
            "location_latitude",
            "location_longitude",
            "config_diabetesManagementEnabled",
            "config_teleconsultationEnabled"
        ) SELECT
            "uuid",
            "name",
            "facilityType",
            "streetAddress",
            "villageOrColony",
            "district",
            "state",
            "country",
            "pinCode",
            "protocolUuid",
            "groupUuid",
            "createdAt",
            "updatedAt",
            "syncStatus",
            "deletedAt",
            '',
            "location_latitude",
            "location_longitude",
            "config_diabetesManagementEnabled",
            "config_teleconsultationEnabled"
          FROM "Facility_OLD"
        """)
      execSQL(""" DROP TABLE "Facility_OLD" """)

      // These are needed because the new version of SQLite has changed
      // the behaviour of ALTER TABLE statements to take Views into
      // account which end up changing the references in the following Views
      execSQL(""" DROP VIEW "OverdueAppointment" """)
      execSQL("CREATE VIEW `OverdueAppointment` AS SELECT P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.assignedFacilityId patientAssignedFacilityUuid,\n\n          A.uuid appt_uuid, A.patientUuid appt_patientUuid, A.facilityUuid appt_facilityUuid, A.scheduledDate appt_scheduledDate, A.status appt_status,\n          A.cancelReason appt_cancelReason, A.remindOn appt_remindOn, A.agreedToVisit appt_agreedToVisit, A.appointmentType appt_appointmentType,\n          A.syncStatus appt_syncStatus, A.createdAt appt_createdAt, A.updatedAt appt_updatedAt, A.creationFacilityUuid appt_creationFacilityUuid,\n\n          PPN.uuid phone_uuid, PPN.patientUuid phone_patientUuid, PPN.number phone_number, PPN.phoneType phone_phoneType, PPN.active phone_active,\n          PPN.createdAt phone_createdAt, PPN.updatedAt phone_updatedAt,\n\n          MH.hasDiabetes diagnosedWithDiabetes, MH.diagnosedWithHypertension diagnosedWithHypertension,\n\n          (\n            CASE\n                WHEN BP.uuid IS NULL THEN BloodSugar.recordedAt\n                WHEN BloodSugar.uuid IS NULL THEN BP.recordedAt\n                ELSE MAX(BP.recordedAt, BloodSugar.recordedAt)\n            END\n          ) AS patientLastSeen,\n\n          (\n            CASE\n              WHEN BloodSugar.reading_type = 'fasting' AND CAST (BloodSugar.reading_value AS REAL) >= 200 THEN 1\n              WHEN BloodSugar.reading_type = 'random' AND CAST (BloodSugar.reading_value AS REAL) >= 300 THEN 1\n              WHEN BloodSugar.reading_type = 'post_prandial' AND CAST (BloodSugar.reading_value AS REAL) >= 300 THEN 1\n              WHEN BloodSugar.reading_type = 'hba1c' AND CAST (BloodSugar.reading_value AS REAL) >= 9 THEN 1\n              WHEN BP.systolic >= 180 OR BP.diastolic >= 110 THEN 1\n              WHEN (MH.hasHadHeartAttack = 'yes' OR MH.hasHadStroke = 'yes') AND (BP.systolic >= 140 OR BP.diastolic >= 110) \n                THEN 1 \n              ELSE 0\n            END\n          ) AS isAtHighRisk,\n          \n          PA.streetAddress patient_address_streetAddress, PA.colonyOrVillage patient_address_colonyOrVillage,\n          PA.district patient_address_district, PA.state patient_address_state,\n          \n          AF.name appointmentFacilityName\n\n          FROM Patient P\n\n          INNER JOIN Appointment A ON A.patientUuid = P.uuid\n          LEFT JOIN PatientPhoneNumber PPN ON (PPN.patientUuid = P.uuid AND PPN.deletedAt IS NULL)\n          LEFT JOIN MedicalHistory MH ON MH.patientUuid = P.uuid\n          LEFT JOIN PatientAddress PA ON PA.uuid = P.addressUuid\n\n          LEFT JOIN (\n            SELECT * FROM BloodPressureMeasurement WHERE deletedAt IS NULL GROUP BY patientUuid HAVING max(recordedAt)\n          ) BP ON BP.patientUuid = P.uuid\n\n          LEFT JOIN (\n            SELECT * FROM BloodSugarMeasurements WHERE deletedAt IS NULL GROUP BY patientUuid HAVING max(recordedAt)\n          ) BloodSugar ON BloodSugar.patientUuid = P.uuid\n          \n          LEFT JOIN Facility AF ON AF.uuid == A.facilityUuid\n          \n          WHERE \n            P.deletedAt IS NULL\n            AND A.deletedAt IS NULL\n            AND A.status = 'scheduled'\n            AND PPN.number IS NOT NULL\n            AND (BP.recordedAt IS NOT NULL OR BloodSugar.recordedAt IS NOT NULL)")

      execSQL(""" DROP VIEW "PatientSearchResult" """)
      execSQL("CREATE VIEW `PatientSearchResult` AS SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.status, P.createdAt, P.updatedAt, P.syncStatus, P.recordedAt,\n  PA.uuid addr_uuid, PA.streetAddress addr_streetAddress, PA.colonyOrVillage addr_colonyOrVillage, PA.zone addr_zone, PA.district addr_district,\n  PA.state addr_state, PA.country addr_country,\n  PA.createdAt addr_createdAt, PA.updatedAt addr_updatedAt,\n  PP.uuid phoneUuid, PP.number phoneNumber, PP.phoneType phoneType, PP.active phoneActive, PP.createdAt phoneCreatedAt, PP.updatedAt phoneUpdatedAt,\n  PatientLastSeen.lastSeenTime lastSeen_lastSeenOn, F.name lastSeen_lastSeenAtFacilityName, PatientLastSeen.lastSeenFacilityUuid lastSeen_lastSeenAtFacilityUuid\n  FROM Patient P\n  INNER JOIN PatientAddress PA ON PA.uuid = P.addressUuid\n  LEFT JOIN PatientPhoneNumber PP ON PP.patientUuid = P.uuid\n  LEFT JOIN (\n      SELECT P.uuid patientUuid,\n      (\n          CASE\n              WHEN LatestBloodSugar.uuid IS NULL THEN LatestBP.recordedAt\n              WHEN LatestBP.uuid IS NULL THEN LatestBloodSugar.recordedAt\n              ELSE MAX(LatestBP.recordedAt, LatestBloodSugar.recordedAt)\n          END\n      ) lastSeenTime,\n      (\n          CASE\n              WHEN LatestBloodSugar.uuid IS NULL THEN LatestBP.facilityUuid\n              WHEN LatestBP.uuid IS NULL THEN LatestBloodSugar.facilityUuid\n              WHEN LatestBP.recordedAt > LatestBloodSugar.recordedAt THEN LatestBP.facilityUuid\n              ELSE LatestBloodSugar.facilityUuid\n          END\n      ) lastSeenFacilityUuid\n      FROM Patient P\n      LEFT JOIN (\n          SELECT BP.uuid, BP.patientUuid, BP.recordedAt, F.uuid facilityUuid FROM BloodPressureMeasurement BP\n          INNER JOIN Facility F ON F.uuid = BP.facilityUuid\n          WHERE BP.deletedAt IS NULL\n          GROUP BY BP.patientUuid HAVING MAX(BP.recordedAt)\n      ) LatestBP ON LatestBP.patientUuid = P.uuid\n      LEFT JOIN (\n          SELECT BloodSugar.uuid, BloodSugar.patientUuid, BloodSugar.recordedAt, F.uuid facilityUuid FROM BloodSugarMeasurements BloodSugar\n          INNER JOIN Facility F ON F.uuid = BloodSugar.facilityUuid\n          WHERE BloodSugar.deletedAt IS NULL\n          GROUP BY BloodSugar.patientUuid HAVING MAX(BloodSugar.recordedAt)\n      ) LatestBloodSugar ON LatestBloodSugar.patientUuid = P.uuid\n      WHERE LatestBP.uuid IS NOT NULL OR LatestBloodSugar.uuid IS NOT NULL\n  ) PatientLastSeen ON PatientLastSeen.patientUuid = P.uuid\n  LEFT JOIN Facility F ON F.uuid = PatientLastSeen.lastSeenFacilityUuid")

      execSQL("PRAGMA foreign_key_check")
    }

    database.execSQL("PRAGMA foreign_keys = ON")
  }
}
