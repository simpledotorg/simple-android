package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration95AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 94,
    toVersion = 95
) {

  @Test
  fun migrating_should_create_the_CallResult_table() {
    before.assertTableDoesNotExist("CallResult")

    after.assertTableExists("CallResult")
  }
}
