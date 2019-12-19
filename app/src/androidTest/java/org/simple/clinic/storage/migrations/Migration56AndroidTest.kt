package org.simple.clinic.storage.migrations

import androidx.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.assertColumnCount
import org.simple.clinic.assertValues
import org.simple.clinic.insert
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class Migration56AndroidTest : BaseDatabaseMigrationTest(fromVersion = 55, toVersion = 56) {

  private val tableName = "Appointment"

  @Test
  fun migrating_from_55_to_56_should_add_creation_facility_uuid_column_in_Appointment() {
    before.assertColumnCount(tableName, 13)

    val uuid = UUID.fromString("00cf4e56-cccf-4fad-b087-a5bacc35d5d0")
    val patientUuid = UUID.fromString("b727faa6-b5cb-4fb4-8ba8-bb3bda86f1f9")
    val facilityUuid = UUID.fromString("74bcfbb1-5a70-4fee-8237-ae454b0b39a0")
    before.insert(tableName, mapOf(
        "uuid" to uuid,
        "patientUuid" to patientUuid,
        "facilityUuid" to facilityUuid,
        "scheduledDate" to "2018-01-01",
        "status" to "SCHEDULED",
        "cancelReason" to null,
        "remindOn" to "2018-01-30",
        "agreedToVisit" to "true",
        "appointmentType" to "manual",
        "syncStatus" to "DONE",
        "createdAt" to "2018-01-01T00:00:00Z",
        "updatedAt" to "2018-01-01T00:00:00Z",
        "deletedAt" to null
    ))

    after.assertColumnCount(tableName, 14)
    after.query("""SELECT * FROM $tableName""").use { cursor ->
      with(cursor) {
        assertThat(count).isEqualTo(1)
        moveToFirst()
        assertValues(mapOf(
            "uuid" to uuid,
            "patientUuid" to patientUuid,
            "facilityUuid" to facilityUuid,
            "creationFacilityUuid" to facilityUuid,
            "scheduledDate" to "2018-01-01",
            "status" to "SCHEDULED",
            "cancelReason" to null,
            "remindOn" to "2018-01-30",
            "agreedToVisit" to "true",
            "appointmentType" to "manual",
            "syncStatus" to "DONE",
            "createdAt" to "2018-01-01T00:00:00Z",
            "updatedAt" to "2018-01-01T00:00:00Z",
            "deletedAt" to null)
        )
      }
    }
  }
}
