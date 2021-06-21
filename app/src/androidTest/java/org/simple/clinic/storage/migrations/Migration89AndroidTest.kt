package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertViewExists

class Migration89AndroidTest : BaseDatabaseMigrationTest(88, 89) {

  @Test
  fun the_overdue_patient_list_should_be_added() {
    before.assertViewExists("OverdueAppointment")
    after.assertViewExists("OverdueAppointment")
  }
}
