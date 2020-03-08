package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists


class Migration55AndroidTest : BaseDatabaseMigrationTest(fromVersion = 54, toVersion = 55) {

  @Test
  fun migrating_to_55_should_generate_the_BloodSugarMeasurements_table() {
    before.assertTableDoesNotExist("BloodSugarMeasurements")

    after.assertTableExists("BloodSugarMeasurements")
  }
}
