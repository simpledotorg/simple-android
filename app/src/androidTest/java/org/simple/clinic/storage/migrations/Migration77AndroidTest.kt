package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertColumns
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

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

  @Test
  fun prescribed_drug_information_should_be_retained_after_the_migration() {
    val drugUuid = UUID.fromString("54b5c22a-84c8-4ba1-95ef-f01d0e547d2f")
    val patientUuid = UUID.fromString("bb3d077f-70d6-426a-b510-fe025b11c7b2")
    val facilityUuid = UUID.fromString("02fa13c4-0192-46ee-95ba-2ace284da267")

    before.insert("PrescribedDrug", mapOf(
        "uuid" to drugUuid,
        "name" to "Sample Drug",
        "dosage" to "25 mg",
        "rxNormCode" to "BD",
        "isDeleted" to false,
        "isProtocolDrug" to false,
        "patientUuid" to patientUuid,
        "facilityUuid" to facilityUuid,
        "syncStatus" to "DONE",
        "createdAt" to Instant.parse("2020-09-23T00:00:00Z"),
        "updatedAt" to Instant.parse("2020-09-23T00:00:00Z"),
        "deletedAt" to null,
        "frequency" to null,
        "durationInDays" to null
    ))

    after.query(""" SELECT * FROM PrescribedDrug """).use { cursor ->
      cursor.moveToNext()
      cursor.assertValues(mapOf(
          "uuid" to drugUuid,
          "name" to "Sample Drug",
          "dosage" to "25 mg",
          "rxNormCode" to "BD",
          "isDeleted" to false,
          "isProtocolDrug" to false,
          "patientUuid" to patientUuid,
          "facilityUuid" to facilityUuid,
          "syncStatus" to "DONE",
          "createdAt" to Instant.parse("2020-09-23T00:00:00Z"),
          "updatedAt" to Instant.parse("2020-09-23T00:00:00Z"),
          "deletedAt" to null,
          "frequency" to null,
          "durationInDays" to null,
          "teleconsultationId" to null
      ))
    }
  }
}
