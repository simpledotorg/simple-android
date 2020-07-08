package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns

class Migration69AndroidTest : BaseDatabaseMigrationTest(68, 69) {

  @Test
  fun registered_facility_id_and_assigned_facility_id_should_be_added_to_patient_table() {
    before.assertColumns("Patient", setOf(
        "uuid",
        "addressUuid",
        "fullName",
        "gender",
        "dateOfBirth",
        "age_value",
        "age_updatedAt",
        "status",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "recordedAt",
        "syncStatus",
        "reminderConsent",
        "deletedReason"
    ))

    after.assertColumns("Patient", setOf(
        "uuid",
        "addressUuid",
        "fullName",
        "gender",
        "dateOfBirth",
        "age_value",
        "age_updatedAt",
        "status",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "recordedAt",
        "syncStatus",
        "reminderConsent",
        "deletedReason",
        "registeredFacilityId",
        "assignedFacilityId"
    ))
  }
}
