package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration73AndroidTest : BaseDatabaseMigrationTest(72, 73) {

  @Test
  fun prescribed_drug_information_should_be_retained_after_the_migration() {
    val drugUuid = UUID.fromString("54b5c22a-84c8-4ba1-95ef-f01d0e547d2f")
    val patientUuid = UUID.fromString("bb3d077f-70d6-426a-b510-fe025b11c7b2")
    val facilityUuid = UUID.fromString("02fa13c4-0192-46ee-95ba-2ace284da267")

    before.insert("PrescribedDrug", mapOf(
        "uuid" to drugUuid,
        "name" to "Sample Drug",
        "dosage" to "10 mg",
        "rxNormCode" to "SD",
        "isDeleted" to false,
        "isProtocolDrug" to false,
        "patientUuid" to patientUuid,
        "facilityUuid" to facilityUuid,
        "syncStatus" to "DONE",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to null
    ))

    after.query(""" SELECT * FROM PrescribedDrug """).use { cursor ->
      cursor.moveToNext()
      cursor.assertValues(mapOf(
          "uuid" to drugUuid,
          "name" to "Sample Drug",
          "dosage" to "10 mg",
          "rxNormCode" to "SD",
          "isDeleted" to false,
          "isProtocolDrug" to false,
          "patientUuid" to patientUuid,
          "facilityUuid" to facilityUuid,
          "syncStatus" to "DONE",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "deletedAt" to null,
          "frequency" to null,
          "durationInDays" to null
      ))
    }
  }
}
