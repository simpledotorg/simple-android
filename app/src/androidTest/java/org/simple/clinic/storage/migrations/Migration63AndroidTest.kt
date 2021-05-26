package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertViewDoesNotExist
import org.simple.clinic.assertViewExists

class Migration63AndroidTest : BaseDatabaseMigrationTest(62, 63) {

  @Test
  fun the_overdue_appointment_view_should_be_added() {
    before.assertViewDoesNotExist("OverdueAppointment")

    after.assertViewExists("OverdueAppointment")
  }
}
