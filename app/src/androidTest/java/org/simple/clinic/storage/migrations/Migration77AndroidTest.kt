package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns

class Migration77AndroidTest : BaseDatabaseMigrationTest(fromVersion = 76, toVersion = 77) {

  @Test
  fun migrating_to_77_should_add_teleconsultation_id_to_prescribed_drug_table() {

    before.assertColumns("PrescribedDrug", setOf(
        "uuid",
        "name",
        "dosage",
        "rxNormCode",
        "isDeleted",
        "isProtocolDrug",
        "patientUuid",
        "facilityUuid",
        "syncStatus",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "frequency",
        "durationInDays"
    ))

    after.assertColumns("PrescribedDrug", setOf(
        "uuid",
        "name",
        "dosage",
        "rxNormCode",
        "isDeleted",
        "isProtocolDrug",
        "patientUuid",
        "facilityUuid",
        "syncStatus",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "frequency",
        "durationInDays",
        "teleconsultationId"
    ))
  }
}
