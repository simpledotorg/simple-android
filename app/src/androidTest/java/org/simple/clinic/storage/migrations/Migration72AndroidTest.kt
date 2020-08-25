package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists

class Migration72AndroidTest : BaseDatabaseMigrationTest(fromVersion = 71, toVersion = 72) {

  @Test
  fun migrating_to_72_should_generate_the_teleconsult_tables() {
    before.assertTableDoesNotExist("TeleconsultationFacilityInfo")
    before.assertTableDoesNotExist("MedicalOfficer")
    before.assertTableDoesNotExist("TeleconsultationFacilityMedicalOfficersCrossRef")

    after.assertTableExists("TeleconsultationFacilityInfo")
    after.assertTableExists("MedicalOfficer")
    after.assertTableExists("TeleconsultationFacilityMedicalOfficersCrossRef")
  }
}
