package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration103AndroidTest : BaseDatabaseMigrationTest(102, 103) {

  @Test
  fun migration_should_create_patient_fts_table() {
    before.assertTableDoesNotExist("PatientAddressFts")

    after.assertTableExists("PatientAddressFts")
  }
}
