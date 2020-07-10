package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration61AndroidTest : BaseDatabaseMigrationTest(60, 61) {

  @Test
  fun migration_to_61_should_convert_the_blood_sugar_reading_from_int_to_string() {
    val bloodSugarMeasurementUuid = UUID.fromString("17290c5f-2e22-47ba-873e-3ce8e0335f5a")
    val patientUuid = UUID.fromString("314478bf-0aac-4258-be20-101c5fd8dd2c")
    val userUuid = UUID.fromString("97be56df-32e4-43e2-a145-c38121a8fbb8")
    val facilityUuid = UUID.fromString("3dafde1b-137d-4524-9603-84bc053cb423")

    before.insert("BloodSugarMeasurements", mapOf(
        "uuid" to bloodSugarMeasurementUuid,
        "reading_value" to 145,
        "reading_type" to "random",
        "patientUuid" to patientUuid,
        "userUuid" to userUuid,
        "facilityUuid" to facilityUuid,
        "recordedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "syncStatus" to "DONE"
    ))

    after
        .query("""SELECT * FROM "BloodSugarMeasurements" WHERE "uuid" == '$bloodSugarMeasurementUuid' """)
        .use { cursor ->
          cursor.moveToFirst()
          cursor.assertValues(mapOf(
              "uuid" to bloodSugarMeasurementUuid,
              "reading_value" to "145",
              "reading_type" to "random",
              "patientUuid" to patientUuid,
              "userUuid" to userUuid,
              "facilityUuid" to facilityUuid,
              "recordedAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "deletedAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "syncStatus" to "DONE"
          ))
        }
  }
}
