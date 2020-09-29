package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns

class Migration79AndroidTest : BaseDatabaseMigrationTest(fromVersion = 78, toVersion = 79) {

  @Test
  fun sync_status_should_be_added_to_the_teleconsult_record_table() {
    before.assertColumns("TeleconsultRecord", setOf(
        "id",
        "patientId",
        "medicalOfficerId",
        "request_requesterId",
        "request_facilityId",
        "request_requestedAt",
        "record_recordedAt",
        "record_teleconsultationType",
        "record_patientTookMedicines",
        "record_patientConsented",
        "record_medicalOfficerNumber",
        "createdAt",
        "updatedAt",
        "deletedAt"
    ))
    after.assertColumns("TeleconsultRecord", setOf(
        "id",
        "patientId",
        "medicalOfficerId",
        "request_requesterId",
        "request_facilityId",
        "request_requestedAt",
        "record_recordedAt",
        "record_teleconsultationType",
        "record_patientTookMedicines",
        "record_patientConsented",
        "record_medicalOfficerNumber",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "syncStatus"
    ))
  }
}
