package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_85 @Inject constructor() : Migration(84, 85) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL(""" DROP VIEW "PatientSearchResult" """)

      execSQL("""
  CREATE VIEW `PatientSearchResult` AS SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt, P.assignedFacilityId, P.status, P.createdAt, P.updatedAt, P.syncStatus, P.recordedAt,
  PA.uuid addr_uuid, PA.streetAddress addr_streetAddress, PA.colonyOrVillage addr_colonyOrVillage, PA.zone addr_zone, PA.district addr_district,
  PA.state addr_state, PA.country addr_country,
  PA.createdAt addr_createdAt, PA.updatedAt addr_updatedAt,
  PP.uuid phoneUuid, PP.number phoneNumber, PP.phoneType phoneType, PP.active phoneActive, PP.createdAt phoneCreatedAt, PP.updatedAt phoneUpdatedAt,
  PatientLastSeen.lastSeenTime lastSeen_lastSeenOn, F.name lastSeen_lastSeenAtFacilityName, PatientLastSeen.lastSeenFacilityUuid lastSeen_lastSeenAtFacilityUuid
  FROM Patient P
  INNER JOIN PatientAddress PA ON PA.uuid = P.addressUuid
  LEFT JOIN PatientPhoneNumber PP ON PP.patientUuid = P.uuid
  LEFT JOIN (
      SELECT P.uuid patientUuid,
      (
          CASE
              WHEN LatestBloodSugar.uuid IS NULL THEN LatestBP.recordedAt
              WHEN LatestBP.uuid IS NULL THEN LatestBloodSugar.recordedAt
              ELSE MAX(LatestBP.recordedAt, LatestBloodSugar.recordedAt)
          END
      ) lastSeenTime,
      (
          CASE
              WHEN LatestBloodSugar.uuid IS NULL THEN LatestBP.facilityUuid
              WHEN LatestBP.uuid IS NULL THEN LatestBloodSugar.facilityUuid
              WHEN LatestBP.recordedAt > LatestBloodSugar.recordedAt THEN LatestBP.facilityUuid
              ELSE LatestBloodSugar.facilityUuid
          END
      ) lastSeenFacilityUuid
      FROM Patient P
      LEFT JOIN (
          SELECT BP.uuid, BP.patientUuid, BP.recordedAt, F.uuid facilityUuid FROM BloodPressureMeasurement BP
          INNER JOIN Facility F ON F.uuid = BP.facilityUuid
          WHERE BP.deletedAt IS NULL
          GROUP BY BP.patientUuid HAVING MAX(BP.recordedAt)
      ) LatestBP ON LatestBP.patientUuid = P.uuid
      LEFT JOIN (
          SELECT BloodSugar.uuid, BloodSugar.patientUuid, BloodSugar.recordedAt, F.uuid facilityUuid FROM BloodSugarMeasurements BloodSugar
          INNER JOIN Facility F ON F.uuid = BloodSugar.facilityUuid
          WHERE BloodSugar.deletedAt IS NULL
          GROUP BY BloodSugar.patientUuid HAVING MAX(BloodSugar.recordedAt)
      ) LatestBloodSugar ON LatestBloodSugar.patientUuid = P.uuid
      WHERE LatestBP.uuid IS NOT NULL OR LatestBloodSugar.uuid IS NOT NULL
  ) PatientLastSeen ON PatientLastSeen.patientUuid = P.uuid
  LEFT JOIN Facility F ON F.uuid = PatientLastSeen.lastSeenFacilityUuid
      """)
    }
  }
}
