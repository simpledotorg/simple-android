package org.simple.clinic

import android.database.Cursor
import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.storage.Migration_10_11
import org.simple.clinic.storage.Migration_11_12
import org.simple.clinic.storage.Migration_12_13
import org.simple.clinic.storage.Migration_13_14
import org.simple.clinic.storage.Migration_14_15
import org.simple.clinic.storage.Migration_15_16
import org.simple.clinic.storage.Migration_16_17
import org.simple.clinic.storage.Migration_6_7
import org.simple.clinic.storage.Migration_7_8
import org.simple.clinic.storage.Migration_8_9
import org.simple.clinic.storage.Migration_9_10

private fun Cursor.string(column: String): String = getString(getColumnIndex(column))
private fun Cursor.boolean(column: String): Boolean = getInt(getColumnIndex(column)) == 1

@RunWith(AndroidJUnit4::class)
class MigrationAndroidTest {

  @Rule
  @JvmField
  val helper = MigrationTestHelperWithForeignConstraints()

  @Test
  fun migration_6_to_7_with_an_existing_user() {
    val db_v6 = helper.createDatabase(version = 6)

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

    val db_v7 = helper.migrateTo(7, Migration_6_7())

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
    helper.createDatabase(version = 6)
    val db_v7 = helper.migrateTo(7, Migration_6_7())

    val cursor = db_v7.query("SELECT * FROM LoggedInUserFacilityMapping")

    cursor.use {
      assertThat(cursor.count).isEqualTo(0)
    }
  }

  @Test
  fun migration_7_to_8() {
    val db_v7 = helper.createDatabase(version = 7)

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

    val db_v8 = helper.migrateTo(8, Migration_7_8())

    val cursor = db_v8.query("""SELECT "loggedInStatus" FROM "LoggedInUser" WHERE "uuid"='c6834f82-3305-4144-9dc8-5f77c908ebf1'""")

    cursor.use {
      assertThat(it.moveToFirst()).isTrue()
      assertThat(it.string("loggedInStatus")).isEqualTo("LOGGED_IN")
    }
  }

  @Test
  fun migration_8_to_9() {
    helper.createDatabase(version = 8)
    val db_v9 = helper.migrateTo(9, Migration_8_9())

    db_v9.query("SELECT * FROM `Appointment`").use {
      assertThat(it.columnCount).isEqualTo(9)
    }
  }

  @Test
  fun migration_9_to_10() {
    helper.createDatabase(version = 9)
    val db_v10 = helper.migrateTo(10, Migration_9_10())

    db_v10.query("SELECT * FROM `Communication`").use {
      assertThat(it.columnCount).isEqualTo(8)
    }
  }

  @Test
  fun migration_10_to_11() {
    val db_v10 = helper.createDatabase(version = 10)

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

    val db_v11 = helper.migrateTo(11, Migration_10_11())

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
    helper.createDatabase(version = 11)
    val db_v12 = helper.migrateTo(12, Migration_11_12())

    db_v12.query("SELECT * FROM `MedicalHistory`").use {
      assertThat(it.columnCount).isEqualTo(10)
    }
  }

  @Test
  fun migration_12_to_13() {
    val db_v12 = helper.createDatabase(version = 12)

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

    val db_v13 = helper.migrateTo(13, Migration_12_13())

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
    val db_v13 = helper.createDatabase(version = 13)

    val patientUuid = "ee367a66-f47e-42d8-965b-7a2b5c54f4bd"
    val addressUuid = "ddb15d83-f390-4f6b-96c5-b2f5064cae6d"

    db_v13.execSQL("""
      INSERT INTO `PatientAddress` VALUES(
        '$addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """)

    db_v13.execSQL("""
      INSERT INTO `Patient` VALUES(
        '$patientUuid',
        '$addressUuid',
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

    val db_v14 = helper.migrateTo(14, Migration_13_14())

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

  @Test
  fun migration_14_to_15() {
    val db_14 = helper.createDatabase(version = 14)

    db_14.query("SELECT * FROM `MedicalHistory`").use {
      assertThat(it.columnCount).isEqualTo(10)
    }

    db_14.execSQL("""
      INSERT OR REPLACE INTO `MedicalHistory` VALUES(
        '464bcda8-b26a-484d-bb70-49b3675f4a38',
        'ee367a66-f47e-42d8-965b-7a2b5c54f4bd',
        0,
        1,
        0,
        1,
        0,
        'IN_FLIGHT',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """)

    val db_v15 = helper.migrateTo(15, Migration_14_15())
    db_v15.query("SELECT * FROM `MedicalHistory`").use {
      assertThat(it.columnCount).isEqualTo(11)

      it.moveToFirst()
      assertThat(it.getString(it.getColumnIndex("uuid"))).isEqualTo("464bcda8-b26a-484d-bb70-49b3675f4a38")
      assertThat(it.getString(it.getColumnIndex("diagnosedWithHypertension"))).isEqualTo("0")
    }
  }

  @Test
  fun migration_15_to_16() {
    val db_v15 = helper.createDatabase(version = 15)

    // We need to do this here because Room does not create virtual tables for us.
    db_v15.execSQL("""CREATE VIRTUAL TABLE "PatientFuzzySearch" USING spellfix1""")

    db_v15.query("""SELECT DISTINCT "tbl_name" FROM "sqlite_master" WHERE "tbl_name"='PatientFuzzySearch'""").use {
      assertThat(it.count).isEqualTo(1)
    }

    val db_v16 = helper.migrateTo(16, Migration_15_16())

    db_v16.query("""SELECT DISTINCT "tbl_name" FROM "sqlite_master" WHERE "tbl_name"='PatientFuzzySearch'""").use {
      assertThat(it.count).isEqualTo(0)
    }
  }

  @Test
  fun migration_16_to_17() {
    val db_16 = helper.createDatabase(version = 16)

    val patientUuid1 = "ee367a66-f47e-42d8-965b-7a2b5c54f4bd"
    val patientUuid2 = "a1d33096-cea6-4beb-8441-82cab2befe2d"
    val addressUuid = "464bcda8-b26a-484d-bb70-49b3675f4a38"

    db_16.execSQL("""
      INSERT INTO `PatientAddress` VALUES(
        '$addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """)

    db_16.execSQL("""
      INSERT INTO `Patient` VALUES(
        '$patientUuid1',
        '$addressUuid',
        'Ash Kumari',
        'AshokKumar',
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

    val db_17 = helper.migrateTo(17, Migration_16_17())

    db_17.execSQL("""
      INSERT INTO `Patient` VALUES(
        '$patientUuid2',
        '$addressUuid',
        'Ash Kumari',
        'AshokKumar',
        'MALE',
        NULL,
        'ACTIVE',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'DONE',
        25,
        '2018-09-25T11:20:42.008Z',
        '1995-09-25');
    """)

    db_17.query("""SELECT * FROM "Patient"""").use {
      assertThat(it.count).isEqualTo(2)
    }
  }
}

