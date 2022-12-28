package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns

class Migration107AndroidTest : BaseDatabaseMigrationTest(fromVersion = 106, toVersion = 107) {

  @Test
  fun deleted_at_columns_should_be_added_to_questionnaire_table() {
    before.assertColumns("Questionnaire", setOf(
        "uuid",
        "questionnaire_type",
        "layout"
    ))

    after.assertColumns("Questionnaire", setOf(
        "uuid",
        "questionnaire_type",
        "layout",
        "deletedAt"
    ))
  }
}
