package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertIndexDoesNotExist
import org.simple.clinic.assertIndexExists

class Migration112AndroidTest : BaseDatabaseMigrationTest(111, 112) {

  @Test
  fun migration_should_create_appointment_id_index_for_call_result_table() {
    before.assertIndexDoesNotExist("index_CallResult_appointmentId")

    after.assertIndexExists("index_CallResult_appointmentId")
  }
}
