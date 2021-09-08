package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns

class Migration96AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 95,
    toVersion = 96
) {

  @Test
  fun migrating_should_add_the_syncStatus_column_to_the_CallResult_table() {
    before.assertColumns("CallResult", setOf(
        "id",
        "userId",
        "appointmentId",
        "removeReason",
        "outcome",
        "createdAt",
        "updatedAt",
        "deletedAt"
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
        "syncStatus"
    ))
  }
}
