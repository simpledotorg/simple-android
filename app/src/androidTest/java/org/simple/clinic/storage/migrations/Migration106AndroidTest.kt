package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration106AndroidTest : BaseDatabaseMigrationTest(fromVersion = 105, toVersion = 106) {

  @Test
  fun migrating_to_106_should_generate_the_Questionnaire_table() {
    before.assertTableDoesNotExist("Questionnaire")

    after.assertTableExists("Questionnaire")
  }
}
