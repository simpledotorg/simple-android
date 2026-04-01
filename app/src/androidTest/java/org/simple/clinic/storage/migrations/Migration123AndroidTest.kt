package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertIndexDoesNotExist
import org.simple.clinic.assertIndexExists
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration123AndroidTest : BaseDatabaseMigrationTest(122, 123) {

  @Test
  fun migration_123_should_generate_the_ReturnScore_table() {
    before.assertTableDoesNotExist("ReturnScore")
    before.assertIndexDoesNotExist("index_ReturnScore_patientUuid")

    after.assertTableExists("ReturnScore")
    after.assertIndexExists("index_ReturnScore_patientUuid")
  }
}
