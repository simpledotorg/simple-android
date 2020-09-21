package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration76AndroidTest : BaseDatabaseMigrationTest(fromVersion = 75, toVersion = 76) {

  @Test
  fun migrating_to_76_should_drop_the_teleconsult_record_with_prescribed_drugs_table() {
    before.assertTableExists("TeleconsultRecordPrescribedDrug")

    after.assertTableDoesNotExist("TeleconsultRecordPrescribedDrug")
  }

}
