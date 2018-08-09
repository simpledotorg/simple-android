package org.simple.clinic

import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory
import android.arch.persistence.room.testing.MigrationTestHelper
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB_NAME = "migration-test"

@RunWith(AndroidJUnit4::class)
class MigrationAndroidTest {

  @Rule
  @JvmField
  val helper: MigrationTestHelper = MigrationTestHelper(
      InstrumentationRegistry.getInstrumentation(),
      AppDatabase::class.java.canonicalName,
      FrameworkSQLiteOpenHelperFactory())

  @Test
  fun migration_6_to_7() {
    val db_v6 = helper.createDatabase(TEST_DB_NAME, 6)

    db_v6.execSQL("""
      INSERT OR ABORT INTO `LoggedInUser`(`uuid`,`fullName`,`phoneNumber`,`pinDigest`,`facilityUuid`,`status`,`createdAt`,`updatedAt`)
      VALUES (
        "c6834f82-3305-4144-9dc8-5f77c908ebf1",
        "Ashok Kumar",
        "1234567890",
        "pinDigest",
        "43dad34c-139e-4e5f-976e-a3ef1d9ac977",
        "APPROVED_FOR_SYNCING",
        "2018-06-21T10:15:58.666Z",
        "2018-06-21T10:15:58.666Z")
    """)

    val db_v7 = helper.runMigrationsAndValidate(TEST_DB_NAME, 7, true, AppDatabase.Migration_6_7())

    val cursor = db_v7.query("SELECT * FROM LoggedInUser")
    assertThat(cursor.count).isEqualTo(1)

    cursor.moveToFirst()
    assertThat(cursor.getString(cursor.getColumnIndex("uuid"))).isEqualTo("c6834f82-3305-4144-9dc8-5f77c908ebf1")
    assertThat(cursor.getString(cursor.getColumnIndex("fullName"))).isEqualTo("Ashok Kumar")
    assertThat(cursor.getString(cursor.getColumnIndex("phoneNumber"))).isEqualTo("1234567890")
    assertThat(cursor.getString(cursor.getColumnIndex("pinDigest"))).isEqualTo("pinDigest")
    assertThat(cursor.getString(cursor.getColumnIndex("facilityUuids"))).isEqualTo("43dad34c-139e-4e5f-976e-a3ef1d9ac977")
    assertThat(cursor.getString(cursor.getColumnIndex("status"))).isEqualTo("APPROVED_FOR_SYNCING")
    assertThat(cursor.getString(cursor.getColumnIndex("createdAt"))).isEqualTo("2018-06-21T10:15:58.666Z")
    assertThat(cursor.getString(cursor.getColumnIndex("updatedAt"))).isEqualTo("2018-06-21T10:15:58.666Z")
  }
}
