package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists
import org.simple.clinic.assertViewExists

class Migration100AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 99,
    toVersion = 100
) {

  @Test
  fun migration_should_create_patient_fts_table() {
    before.assertTableDoesNotExist("PatientFts")

    after.assertTableExists("PatientFts")
  }

  @Test
  fun migration_should_create_patient_phone_number_fts_table() {
    before.assertTableDoesNotExist("PatientPhoneNumberFts")

    after.assertTableExists("PatientPhoneNumberFts")
  }

  @Test
  fun migration_should_create_business_id_fts_table() {
    before.assertTableDoesNotExist("BusinessIdFts")

    after.assertTableExists("BusinessIdFts")
  }

  @Test
  fun patient_search_results_table_should_be_added() {
    before.assertViewExists("PatientSearchResult")
    before.assertColumns("PatientSearchResult", setOf(
        "uuid",
        "fullName",
        "gender",
        "age_value",
        "age_updatedAt",
        "dateOfBirth",
        "assignedFacilityId",
        "status",
        "createdAt",
        "updatedAt",
        "recordedAt",
        "syncStatus",
        "addr_uuid",
        "addr_streetAddress",
        "addr_colonyOrVillage",
        "addr_zone",
        "addr_district",
        "addr_state",
        "addr_country",
        "addr_createdAt",
        "addr_updatedAt",
        "phoneUuid",
        "phoneNumber",
        "phoneType",
        "phoneActive",
        "phoneCreatedAt",
        "phoneUpdatedAt",
        "lastSeen_lastSeenOn",
        "lastSeen_lastSeenAtFacilityName",
        "lastSeen_lastSeenAtFacilityUuid",
        "id_identifier",
        "id_identifierType",
        "identifierSearchHelp"
    ))

    after.assertViewExists("PatientSearchResult")
    after.assertColumns("PatientSearchResult", setOf(
        "uuid",
        "fullName",
        "gender",
        "age_value",
        "age_updatedAt",
        "dateOfBirth",
        "status",
        "assignedFacilityId",
        "assignedFacilityName",
        "addr_uuid",
        "addr_streetAddress",
        "addr_colonyOrVillage",
        "addr_zone",
        "addr_district",
        "addr_state",
        "addr_country",
        "addr_createdAt",
        "addr_updatedAt",
        "addr_deletedAt",
        "phoneNumber",
        "id_identifier",
        "id_identifierType",
        "identifierSearchHelp"
    ))
  }
}
