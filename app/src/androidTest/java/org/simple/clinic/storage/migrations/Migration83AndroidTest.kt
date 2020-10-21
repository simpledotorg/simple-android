package org.simple.clinic.storage.migrations

import org.junit.Test
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.time.Instant
import java.util.UUID

class Migration83AndroidTest : BaseDatabaseMigrationTest(82, 83) {

  @Test
  fun should_rename_request_completed_column_to_requester_completion_status() {
    val teleconsultRecordId = UUID.fromString("a277e974-9cb6-42f4-a4f3-a65471b737d5")
    val patientId = UUID.fromString("b152c4a0-2b90-4f8f-b3c8-0572ae719a5c")
    val medicalOfficerId = UUID.fromString("8a2ddaf3-6277-4e02-bb32-69f143d97894")
    val requesterId = UUID.fromString("793754f0-9ff1-4441-85dd-e0aa303bbc1c")
    val facilityId = UUID.fromString("758de9b9-cca3-45fd-9bd3-ec71ffac57f6")

    before.insert("TeleconsultRecord", mapOf(
        "id" to teleconsultRecordId,
        "patientId" to patientId,
        "medicalOfficerId" to medicalOfficerId,
        "request_requesterId" to requesterId,
        "request_facilityId" to facilityId,
        "request_requestedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "request_requestCompleted" to "waiting",
        "record_recordedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "record_teleconsultationType" to "audio",
        "record_patientTookMedicines" to "yes",
        "record_patientConsented" to "yes",
        "record_medicalOfficerNumber" to null,
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "deletedAt" to null,
        "syncStatus" to "DONE"
    ))

    after.query(""" SELECT * FROM TeleconsultRecord """)
        .use { cursor ->
          cursor.moveToFirst()

          cursor.assertValues(mapOf(
              "id" to teleconsultRecordId,
              "patientId" to patientId,
              "medicalOfficerId" to medicalOfficerId,
              "request_requesterId" to requesterId,
              "request_facilityId" to facilityId,
              "request_requestedAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "request_requesterCompletionStatus" to "waiting",
              "record_recordedAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "record_teleconsultationType" to "audio",
              "record_patientTookMedicines" to "yes",
              "record_patientConsented" to "yes",
              "record_medicalOfficerNumber" to null,
              "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
              "deletedAt" to null,
              "syncStatus" to "DONE",
          ))
        }
  }
}
