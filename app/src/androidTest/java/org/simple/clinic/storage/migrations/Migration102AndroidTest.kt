package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertIndexDoesNotExist
import org.simple.clinic.assertIndexExists
import org.simple.clinic.assertViewDoesNotExist
import org.simple.clinic.assertViewExists

class Migration102AndroidTest : BaseDatabaseMigrationTest(fromVersion = 101, toVersion = 102) {

  @Test
  fun migration_should_drop_overdue_appointment_database_view() {
    before.assertViewExists("OverdueAppointment")

    after.assertViewDoesNotExist("OverdueAppointment")
  }

  @Test
  fun migration_should_add_assigned_facility_index_for_patient_table() {
    before.assertIndexDoesNotExist("index_Patient_assignedFacilityId")

    after.assertIndexExists("index_Patient_assignedFacilityId")
  }
}
