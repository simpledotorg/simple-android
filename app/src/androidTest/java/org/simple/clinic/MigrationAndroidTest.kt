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
import org.simple.clinic.storage.Migration_10_11
import org.simple.clinic.storage.Migration_11_12
import org.simple.clinic.storage.Migration_12_13
import org.simple.clinic.storage.Migration_13_14
import org.simple.clinic.storage.Migration_6_7
import org.simple.clinic.storage.Migration_7_8
import org.simple.clinic.storage.Migration_8_9
import org.simple.clinic.storage.Migration_9_10

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

    val db_v7 = helper.runMigrationsAndValidate(TEST_DB_NAME, 7, true, Migration_6_7())

    val cursor = db_v7.query("SELECT * FROM `LoggedInUserFacilityMapping`")
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
    val db_v7 = helper.runMigrationsAndValidate(TEST_DB_NAME, 7, true, Migration_6_7())

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

    val db_v8 = helper.runMigrationsAndValidate(TEST_DB_NAME, 8, true, Migration_7_8())

    val cursor = db_v8.query("""SELECT "loggedInStatus" FROM "LoggedInUser" WHERE "uuid"='c6834f82-3305-4144-9dc8-5f77c908ebf1'""")

    cursor.use {
      assertThat(it.moveToFirst()).isTrue()
      assertThat(it.string("loggedInStatus")).isEqualTo("LOGGED_IN")
    }
  }

  @Test
  fun migration_8_to_9() {
    helper.createDatabase(TEST_DB_NAME, 8)
    val db_v9 = helper.runMigrationsAndValidate(TEST_DB_NAME, 9, true, Migration_8_9())

    db_v9.query("SELECT * FROM `Appointment`").use {
      assertThat(it.columnCount).isEqualTo(9)
    }
  }

  @Test
  fun migration_9_to_10() {
    helper.createDatabase(TEST_DB_NAME, 9)
    val db_v10 = helper.runMigrationsAndValidate(TEST_DB_NAME, 10, true, Migration_9_10())

    db_v10.query("SELECT * FROM `Communication`").use {
      assertThat(it.columnCount).isEqualTo(8)
    }
  }

  @Test
  fun migration_10_to_11() {
    val db_v10 = helper.createDatabase(TEST_DB_NAME, 10)

    db_v10.execSQL("""
      INSERT OR REPLACE INTO `Appointment`(`id`,`patientId`,`facilityId`,`date`,`status`,`statusReason`,`syncStatus`,`createdAt`,`updatedAt`)
      VALUES (
        'c6834f82-3305-4144-9dc8-5f77c908ebf1',
        'a1d33096-cea6-4beb-8441-82cab2befe2d',
        '0274a4a6-dd0e-493c-86aa-6502cd1fc2a0',
        '2011-12-03',
        'SCHEDULED',
        'NOT_CALLED_YET',
        'PENDING',
        '2018-06-21T10:15:58.666Z',
        '2018-06-21T10:15:58.666Z');
    """)

    db_v10.execSQL("""
      INSERT OR REPLACE INTO `Communication`(`id`,`appointmentId`,`userId`,`type`,`result`,`syncStatus`,`createdAt`,`updatedAt`)
      VALUES (
        'afea327f-4286-417b-9271-945ef2c7592a',
        'c6834f82-3305-4144-9dc8-5f77c908ebf1',
        'c64f76b5-0d37-46e2-9426-554e4f809498',
        'MANUAL_CALL',
        'AGREED_TO_VISIT',
        'PENDING',
        '2018-06-21T10:15:58.666Z',
        '2018-06-21T10:15:58.666Z');
    """)

    val db_v11 = helper.runMigrationsAndValidate(TEST_DB_NAME, 11, true, Migration_10_11())

    db_v11.query("SELECT * FROM `Appointment`").use {
      assertThat(it.count).isEqualTo(1)

      it.moveToFirst()
      assertThat(it.columnNames.contains("id")).isFalse()
      assertThat(it.columnNames.contains("patientId")).isFalse()
      assertThat(it.columnNames.contains("facilityId")).isFalse()

      assertThat(it.string("uuid")).isEqualTo("c6834f82-3305-4144-9dc8-5f77c908ebf1")
      assertThat(it.string("patientUuid")).isEqualTo("a1d33096-cea6-4beb-8441-82cab2befe2d")
      assertThat(it.string("facilityUuid")).isEqualTo("0274a4a6-dd0e-493c-86aa-6502cd1fc2a0")
    }

    db_v11.query("SELECT * FROM `Communication`").use {
      assertThat(it.count).isEqualTo(1)

      it.moveToFirst()
      assertThat(it.columnNames.contains("id")).isFalse()
      assertThat(it.columnNames.contains("appointmentId")).isFalse()
      assertThat(it.columnNames.contains("userId")).isFalse()

      assertThat(it.string("uuid")).isEqualTo("afea327f-4286-417b-9271-945ef2c7592a")
      assertThat(it.string("appointmentUuid")).isEqualTo("c6834f82-3305-4144-9dc8-5f77c908ebf1")
      assertThat(it.string("userUuid")).isEqualTo("c64f76b5-0d37-46e2-9426-554e4f809498")
    }
  }

  @Test
  fun migration_11_to_12() {
    helper.createDatabase(TEST_DB_NAME, 11)
    val db_v12 = helper.runMigrationsAndValidate(TEST_DB_NAME, 12, true, Migration_11_12())

    db_v12.query("SELECT * FROM `MedicalHistory`").use {
      assertThat(it.columnCount).isEqualTo(10)
    }
  }

  @Test
  fun migration_12_to_13() {
    val db_v12 = helper.createDatabase(TEST_DB_NAME, 12)

    db_v12.execSQL("""
      INSERT OR REPLACE INTO `Appointment`(`uuid`,`patientUuid`,`facilityUuid`,`date`,`status`,`statusReason`,`syncStatus`,`createdAt`,`updatedAt`)
      VALUES (
        'c6834f82-3305-4144-9dc8-5f77c908ebf1',
        'a1d33096-cea6-4beb-8441-82cab2befe2d',
        '0274a4a6-dd0e-493c-86aa-6502cd1fc2a0',
        '2011-12-03',
        'SCHEDULED',
        'NOT_CALLED_YET',
        'PENDING',
        '2018-06-21T10:15:58.666Z',
        '2018-06-21T10:15:58.666Z');
    """)

    val db_v13 = helper.runMigrationsAndValidate(TEST_DB_NAME, 13, true, Migration_12_13())

    db_v13.query("SELECT * FROM `Appointment`").use {
      assertThat(it.count).isEqualTo(1)

      it.moveToFirst()
      assertThat(it.columnNames.contains("date")).isFalse()
      assertThat(it.columnNames.contains("statusReason")).isFalse()
      assertThat(it.columnNames.contains("scheduledDate")).isTrue()
      assertThat(it.columnNames.contains("cancelReason")).isTrue()
      assertThat(it.columnNames.contains("remindOn")).isTrue()
      assertThat(it.columnNames.contains("agreedToVisit")).isTrue()

      assertThat(it.string("uuid")).isEqualTo("c6834f82-3305-4144-9dc8-5f77c908ebf1")
      assertThat(it.string("scheduledDate")).isEqualTo("2011-12-03")
      assertThat(it.isNull(it.getColumnIndex("cancelReason"))).isTrue()
    }
  }

  @Test
  fun migration_13_to_14() {
    val db_v13 = helper.createDatabase(TEST_DB_NAME, 13)

    val patientUuid = "ee367a66-f47e-42d8-965b-7a2b5c54f4bd"

    db_v13.execSQL("""
      INSERT INTO `Patient` VALUES(
        '$patientUuid',
        '464bcda8-b26a-484d-bb70-49b3675f4a38',
        'Ash Kumari','AshokKumar',
        'FEMALE',
        NULL,
        'ACTIVE',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'DONE',
        23,
        '2018-09-25T11:20:42.008Z',
        '1995-09-25');
    """)

    db_v13.execSQL("""
      INSERT INTO `MedicalHistory` VALUES(
        'old-uuid',
        '$patientUuid',
        0,
        0,
        1,
        0,
        1,
        'DONE',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z'
      )
    """)

    db_v13.query("SELECT * FROM `MedicalHistory` WHERE patientUuid = '$patientUuid'").use {
      assertThat(it.count).isEqualTo(1)
    }

    val db_v14 = helper.runMigrationsAndValidate(TEST_DB_NAME, 14, true, Migration_13_14())

    db_v14.query("SELECT * FROM `MedicalHistory` WHERE patientUuid = '$patientUuid'").use {
      assertThat(it.count).isEqualTo(1)
      it.moveToFirst()

      val falseAsInt = 0
      assertThat(it.getString(it.getColumnIndex("uuid"))).isNotEqualTo("old-uuid")
      assertThat(it.getInt(it.getColumnIndex("hasHadHeartAttack"))).isEqualTo(falseAsInt)
      assertThat(it.getInt(it.getColumnIndex("hasHadStroke"))).isEqualTo(falseAsInt)
      assertThat(it.getInt(it.getColumnIndex("hasHadKidneyDisease"))).isEqualTo(falseAsInt)
      assertThat(it.getInt(it.getColumnIndex("isOnTreatmentForHypertension"))).isEqualTo(falseAsInt)
      assertThat(it.getInt(it.getColumnIndex("hasDiabetes"))).isEqualTo(falseAsInt)
    }
  }
}
