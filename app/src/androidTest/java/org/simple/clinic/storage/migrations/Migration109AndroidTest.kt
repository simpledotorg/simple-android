package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns

class Migration109AndroidTest : BaseDatabaseMigrationTest(fromVersion = 108, toVersion = 109) {

  @Test
  fun changing_lastUpdatedByUserId_data_type_should_work_for_QuestionnaireResponse_table() {
    before.assertColumns("QuestionnaireResponse", setOf(
        "uuid",
        "questionnaireId",
        "questionnaireType",
        "facilityId",
        "lastUpdatedByUserId",
        "content",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "syncStatus",
    ))

    after.assertColumns("QuestionnaireResponse", setOf(
        "uuid",
        "questionnaireId",
        "questionnaireType",
        "facilityId",
        "lastUpdatedByUserId",
        "content",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "syncStatus",
    ))
  }
}
