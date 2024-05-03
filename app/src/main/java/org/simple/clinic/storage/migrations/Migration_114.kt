package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_114 @Inject constructor() : Migration(113, 114) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL(""" DROP VIEW "PatientSearchResult" """)
      execSQL("""
  CREATE VIEW `PatientSearchResult` AS SELECT P.uuid, P.fullName, P.gender, P.dateOfBirth, P.age_value, P.age_updatedAt,
  P.assignedFacilityId, P.status, P.isEligibleForReassignment,
  PA.uuid addr_uuid, PA.streetAddress addr_streetAddress, PA.colonyOrVillage addr_colonyOrVillage, PA.zone addr_zone, PA.district addr_district,
  PA.state addr_state, PA.country addr_country,
  PA.createdAt addr_createdAt, PA.updatedAt addr_updatedAt, PA.deletedAt addr_deletedAt,
  PP.number phoneNumber,
  B.identifier id_identifier, B.identifierType id_identifierType, B.searchHelp identifierSearchHelp, AF.name assignedFacilityName
  FROM Patient P
  INNER JOIN PatientAddress PA ON PA.uuid = P.addressUuid
  LEFT JOIN PatientPhoneNumber PP ON PP.patientUuid = P.uuid
  LEFT JOIN Facility AF ON AF.uuid = P.assignedFacilityId
  LEFT JOIN BusinessId B ON B.patientUuid = P.uuid
  WHERE P.deletedAt IS NULL
""")
    }
  }
}
