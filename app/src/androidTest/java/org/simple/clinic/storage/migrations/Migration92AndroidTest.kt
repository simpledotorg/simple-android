package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration92AndroidTest: BaseDatabaseMigrationTest(91, 92) {

  @Test
  fun migration_should_create_drugs_table() {
    before.assertTableDoesNotExist("Drug")

    after.assertTableExists("Drug")
  }
}
