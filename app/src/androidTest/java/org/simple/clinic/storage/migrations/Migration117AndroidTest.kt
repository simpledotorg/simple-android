package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertIndexDoesNotExist
import org.simple.clinic.assertIndexExists
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration117AndroidTest : BaseDatabaseMigrationTest(fromVersion = 116, toVersion = 117) {

  @Test
  fun migration_117_should_generate_the_PatientAttribute_table() {
    before.assertTableDoesNotExist("PatientAttribute")
    before.assertIndexDoesNotExist("index_PatientAttribute_patientUuid")

    after.assertTableExists("PatientAttribute")
    after.assertIndexExists("index_PatientAttribute_patientUuid")
  }
}
