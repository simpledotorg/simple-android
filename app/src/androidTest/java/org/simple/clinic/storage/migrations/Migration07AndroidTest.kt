package org.simple.clinic.storage.migrations

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.boolean
import org.simple.clinic.string

class Migration07AndroidTest : BaseDatabaseMigrationTest(
    fromVersion = 6,
    toVersion = 7
) {

  @Test
  fun migration_6_to_7_with_an_existing_user() {
    val facilityUuid = "43dad34c-139e-4e5f-976e-a3ef1d9ac977"

    before.execSQL("""
      INSERT INTO "Facility" (
        "uuid", "name", "facilityType", 
        "streetAddress", "villageOrColony", 
        "district", "state", "country", "pinCode",
        "createdAt", "updatedAt", "syncStatus"
      ) VALUES (
        "$facilityUuid", "PHC Obvious", "PHC",
        "Richmond Road", "Bangalore Central",
        "Bangalore", "Karnataka", "India", "560038",
        "2018-01-01T00:00:00Z", "2018-01-01T00:00:00Z", "DONE"
      )
    """)

    before.execSQL("""
      INSERT OR ABORT INTO "LoggedInUser"("uuid","fullName","phoneNumber","pinDigest","facilityUuid","status","createdAt","updatedAt")
      VALUES (
        'c6834f82-3305-4144-9dc8-5f77c908ebf1',
        'Ashok Kumar',
        '1234567890',
        'pinDigest',
        '$facilityUuid',
        'APPROVED_FOR_SYNCING',
        '2018-06-21T10:15:58.666Z',
        '2018-06-21T10:15:58.666Z')
    """)

    val cursor = after.query(""" SELECT * FROM "LoggedInUserFacilityMapping" """)
    cursor.use {
      assertThat(cursor.count).isEqualTo(1)

      it.moveToFirst()
      assertThat(it.string("userUuid")).isEqualTo("c6834f82-3305-4144-9dc8-5f77c908ebf1")
      assertThat(it.string("facilityUuid")).isEqualTo(facilityUuid)
      assertThat(it.boolean("isCurrentFacility")).isTrue()
    }

    after.query("SELECT * FROM LoggedInUser").use {
      assertThat(it.columnNames.contains("facilityUuid")).isFalse()
    }
  }

  @Test
  fun migration_6_to_7_without_an_existing_user() {
    before.query(""" SELECT * FROM "LoggedInUser" """).use { cursor ->
      assertThat(cursor.count).isEqualTo(0)
    }

    val cursor = after.query("SELECT * FROM LoggedInUserFacilityMapping")

    cursor.use {
      assertThat(cursor.count).isEqualTo(0)
    }
  }
}
