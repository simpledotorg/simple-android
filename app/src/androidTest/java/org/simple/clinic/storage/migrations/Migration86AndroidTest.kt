package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertViewExists

class Migration86AndroidTest : BaseDatabaseMigrationTest(85, 86) {

  @Test
  fun the_patient_search_result_view_should_be_added() {
    before.assertViewExists("PatientSearchResult")
    after.assertViewExists("PatientSearchResult")
  }
}

