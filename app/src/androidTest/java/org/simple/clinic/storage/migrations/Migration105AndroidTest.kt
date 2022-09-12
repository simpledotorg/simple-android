package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns

class Migration105AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 104,
    toVersion = 105
) {

  @Test
  fun patient_id_and_facility_id_columns_should_be_added_to_call_result_table() {
    before.assertColumns("CallResult", setOf(
        "id",
        "userId",
        "appointmentId",
        "removeReason",
        "outcome",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "syncStatus"
    ))

    after.assertColumns("CallResult", setOf(
        "id",
        "userId",
        "appointmentId",
        "removeReason",
        "outcome",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "syncStatus",
        "patientId",
        "facilityId"
    ))
  }
}
