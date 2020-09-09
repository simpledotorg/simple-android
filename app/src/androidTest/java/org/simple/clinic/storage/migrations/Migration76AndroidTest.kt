package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration76AndroidTest: BaseDatabaseMigrationTest(
    fromVersion = 75,
    toVersion = 76
) {

  @Test
  fun the_materialized_overdue_appointment_table_should_be_created() {
    before.assertTableDoesNotExist("OverdueAppointment_New")

    after.assertTableExists("OverdueAppointment_New")
  }
}
