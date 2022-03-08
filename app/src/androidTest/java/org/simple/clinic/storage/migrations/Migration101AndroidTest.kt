package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertIndexDoesNotExist
import org.simple.clinic.assertIndexExists

class Migration101AndroidTest : BaseDatabaseMigrationTest(fromVersion = 100, toVersion = 101) {

  @Test
  fun migration_should_add_indexes() {
    before.assertIndexDoesNotExist("index_BloodPressureMeasurement_facilityUuid")
    before.assertIndexDoesNotExist("index_BloodSugarMeasurements_facilityUuid")
    before.assertIndexDoesNotExist("index_PrescribedDrug_facilityUuid")
    before.assertIndexDoesNotExist("index_Appointment_creationFacilityUuid")

    after.assertIndexExists("index_BloodPressureMeasurement_facilityUuid")
    after.assertIndexExists("index_BloodSugarMeasurements_facilityUuid")
    after.assertIndexExists("index_PrescribedDrug_facilityUuid")
    after.assertIndexExists("index_Appointment_creationFacilityUuid")
  }
}
