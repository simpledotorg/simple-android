package org.simple.clinic

import android.arch.persistence.room.testing.MigrationTestHelper
import android.database.Cursor
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.di.AppSqliteOpenHelperFactory

private const val TEST_DB_NAME = "migration-test"

private fun Cursor.string(column: String): String = getString(getColumnIndex(column))
private fun Cursor.boolean(column: String): Boolean = getInt(getColumnIndex(column)) == 1

@RunWith(AndroidJUnit4::class)
class MigrationAndroidTest {

  @Rule
  @JvmField
  val helper: MigrationTestHelper = MigrationTestHelper(
      InstrumentationRegistry.getInstrumentation(),
      AppDatabase::class.java.canonicalName,
      AppSqliteOpenHelperFactory())

  @Test
  fun migration_6_to_7_with_an_existing_user() {
    val db_v6 = helper.createDatabase(TEST_DB_NAME, 6)

    db_v6.execSQL("""
      INSERT OR ABORT INTO `LoggedInUser`(`uuid`,`fullName`,`phoneNumber`,`pinDigest`,`facilityUuid`,`status`,`createdAt`,`updatedAt`)
      VALUES (
        'c6834f82-3305-4144-9dc8-5f77c908ebf1',
        'Ashok Kumar',
        '1234567890',
        'pinDigest',
        '43dad34c-139e-4e5f-976e-a3ef1d9ac977',
        'APPROVED_FOR_SYNCING',
        '2018-06-21T10:15:58.666Z',
        '2018-06-21T10:15:58.666Z')
    """)

    val db_v7 = helper.runMigrationsAndValidate(TEST_DB_NAME, 7, true, AppDatabase.Migration_6_7())

    val cursor = db_v7.query("SELECT * FROM LoggedInUserFacilityMapping")
    cursor.use {
      assertThat(cursor.count).isEqualTo(1)

      it.moveToFirst()
      Truth.assertThat(it.string("userUuid")).isEqualTo("c6834f82-3305-4144-9dc8-5f77c908ebf1")
      Truth.assertThat(it.string("facilityUuid")).isEqualTo("43dad34c-139e-4e5f-976e-a3ef1d9ac977")
      Truth.assertThat(it.boolean("isCurrentFacility")).isTrue()
    }

    db_v7.query("SELECT * FROM LoggedInUser").use {
      assertThat(it.columnNames.contains("facilityUuid")).isFalse()
    }
  }

  @Test
  fun migration_6_to_7_without_an_existing_user() {
    helper.createDatabase(TEST_DB_NAME, 6)
    val db_v7 = helper.runMigrationsAndValidate(TEST_DB_NAME, 7, true, AppDatabase.Migration_6_7())

    val cursor = db_v7.query("SELECT * FROM LoggedInUserFacilityMapping")

    cursor.use {
      assertThat(cursor.count).isEqualTo(0)
    }
  }

  @Test
  fun migration_7_to_8() {
    val db_v7 = helper.createDatabase(TEST_DB_NAME, 7)

    db_v7.execSQL("""
      INSERT OR ABORT INTO `LoggedInUser`(`uuid`,`fullName`,`phoneNumber`,`pinDigest`, `status`,`createdAt`,`updatedAt`)
      VALUES (
        'c6834f82-3305-4144-9dc8-5f77c908ebf1',
        'Ashok Kumar',
        '1234567890',
        'pinDigest',
        'APPROVED_FOR_SYNCING',
        '2018-06-21T10:15:58.666Z',
        '2018-06-21T10:15:58.666Z')
    """)

    val db_v8 = helper.runMigrationsAndValidate(TEST_DB_NAME, 8, true, AppDatabase.Migration_7_8())

    val cursor = db_v8.query("""SELECT "loggedInStatus" FROM "LoggedInUser" WHERE "uuid"='c6834f82-3305-4144-9dc8-5f77c908ebf1'""")

    cursor.use {
      assertThat(it.moveToFirst()).isTrue()
      assertThat(it.string("loggedInStatus")).isEqualTo("LOGGED_IN")
    }
  }
}
