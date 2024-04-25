package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns
import org.simple.clinic.assertViewExists

class Migration114AndroidTest : BaseDatabaseMigrationTest(113, 114) {

  @Test
  fun the_patient_search_result_view_should_be_added() {
    before.assertViewExists("PatientSearchResult")
    before.assertColumns("PatientSearchResult", setOf(
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
        "identifierSearchHelp",
        "isEligibleForReassignment"
    ))
  }
}

