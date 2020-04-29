package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertViewDoesNotExist
import org.simple.clinic.assertViewExists

class Migration64AndroidTest : BaseDatabaseMigrationTest(63, 64) {

  @Test
  fun the_patient_search_result_view_should_be_added() {
    before.assertViewDoesNotExist("PatientSearchResult")

    after.assertViewExists("PatientSearchResult")
  }
}
