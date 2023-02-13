package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration108AndroidTest : BaseDatabaseMigrationTest(fromVersion = 107, toVersion = 108) {

  @Test
  fun migrating_to_108_should_generate_the_QuestionnaireResponse_table() {
    before.assertTableDoesNotExist("QuestionnaireResponse")

    after.assertTableExists("QuestionnaireResponse")
  }
}
