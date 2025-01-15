package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration119AndroidTest : BaseDatabaseMigrationTest(fromVersion = 118, toVersion = 119) {

  @Test
  fun migration_119_should_update_the_CVDRisk_table() {
    val patientId = UUID.fromString("939edc81-da26-49ae-a907-e1c5a5aa9d12")
    val uuid = UUID.fromString("90984b45-fe45-4dde-a34b-285971fc71f7")

    before.insert("CVDRisk", mapOf(
        "uuid" to uuid,
        "patientUuid" to patientId,
        "riskScore" to "7 - 14",
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to null,
        "syncStatus" to "DONE",
    ))

    after
        .query("""SELECT * FROM "CVDRisk" """)
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(1)

          cursor.moveToFirst()
          cursor.assertValues(mapOf(
              "uuid" to uuid,
              "patientUuid" to patientId,
              "min" to 7,
              "max" to 14,
              "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "deletedAt" to null,
              "syncStatus" to "DONE",
          ))
        }
  }
}
