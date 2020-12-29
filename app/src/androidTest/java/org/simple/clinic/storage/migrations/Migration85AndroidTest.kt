package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertViewExists

class Migration85AndroidTest : BaseDatabaseMigrationTest(84, 85) {

  @Test
  fun the_patient_search_result_view_should_be_added() {
    before.assertViewExists("PatientSearchResult")
    after.assertViewExists("PatientSearchResult")
  }
}
