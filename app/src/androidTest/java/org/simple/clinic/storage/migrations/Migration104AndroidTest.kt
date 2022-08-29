package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertIndexDoesNotExist
import org.simple.clinic.assertIndexExists

class Migration104AndroidTest : BaseDatabaseMigrationTest(103, 104) {

  @Test
  fun migration_should_create_facility_uuid_index_for_appointment_table() {
    before.assertIndexDoesNotExist("index_Appointment_facilityUuid")

    after.assertIndexExists("index_Appointment_facilityUuid")
  }
}
