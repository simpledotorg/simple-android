package org.simple.clinic

import android.arch.persistence.room.testing.MigrationTestHelper
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.di.AppSqliteOpenHelperFactory

private const val TEST_DB_NAME = "migration-test"

@RunWith(AndroidJUnit4::class)
class MigrationAndroidTest {

  @Rule
  @JvmField
  val helper: MigrationTestHelper = MigrationTestHelper(
      InstrumentationRegistry.getInstrumentation(),
      AppDatabase::class.java.canonicalName,
      AppSqliteOpenHelperFactory())

  @Test
  fun migration_6_to_7() {
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

    val cursor = db_v7.query("SELECT * FROM LoggedInUser")
    assertThat(cursor.count).isEqualTo(1)

    cursor.use {
      val string: (String) -> String = { column -> it.getString(it.getColumnIndex(column)) }

      cursor.moveToFirst()
      assertThat(string("uuid")).isEqualTo("c6834f82-3305-4144-9dc8-5f77c908ebf1")
      assertThat(string("fullName")).isEqualTo("Ashok Kumar")
      assertThat(string("phoneNumber")).isEqualTo("1234567890")
      assertThat(string("pinDigest")).isEqualTo("pinDigest")
      assertThat(string("facilityUuids")).isEqualTo("43dad34c-139e-4e5f-976e-a3ef1d9ac977")
      assertThat(string("status")).isEqualTo("APPROVED_FOR_SYNCING")
      assertThat(string("createdAt")).isEqualTo("2018-06-21T10:15:58.666Z")
      assertThat(string("updatedAt")).isEqualTo("2018-06-21T10:15:58.666Z")
    }
  }
}
