package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertIndexDoesNotExist
import org.simple.clinic.assertIndexExists
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration118AndroidTest : BaseDatabaseMigrationTest(fromVersion = 117, toVersion = 118) {

  @Test
  fun migration_118_should_generate_the_CVDRisk_table() {
    before.assertTableDoesNotExist("CVDRisk")
    before.assertIndexDoesNotExist("index_CVDRisk_patientUuid")

    after.assertTableExists("CVDRisk")
    after.assertIndexExists("index_CVDRisk_patientUuid")
  }
}
