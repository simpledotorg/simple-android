package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration74AndroidTest : BaseDatabaseMigrationTest(fromVersion = 73, toVersion = 74) {

  @Test
  fun migrating_to_74_should_generate_the_teleconsult_record_tables() {
    before.assertTableDoesNotExist("TeleconsultRecord")
    before.assertTableDoesNotExist("TeleconsultRecordPrescribedDrug")

    after.assertTableExists("TeleconsultRecord")
    after.assertTableExists("TeleconsultRecordPrescribedDrug")
  }
}
