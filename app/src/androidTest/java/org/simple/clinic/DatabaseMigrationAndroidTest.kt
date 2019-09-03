package org.simple.clinic

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.simple.clinic.storage.inTransaction
import org.simple.clinic.user.User
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.UtcClock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@Suppress("LocalVariableName")
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationAndroidTest {

  @get:Rule
  val helper = MigrationTestHelperWithForeignKeyConstraints()

  @get:Rule
  val expectedException = ExpectedException.none()

  @Inject
  lateinit var migrations: ArrayList<Migration>

  @Inject
  @field:Named("last_facility_pull_token")
  lateinit var lastFacilityPullToken: Preference<Optional<String>>

  @Inject
  @field:Named("last_patient_pull_token")
  lateinit var lastPatientPullToken: Preference<Optional<String>>

  @Inject
  lateinit var clock: UtcClock

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    (clock as TestUtcClock).setDate(LocalDate.of(2000, Month.JANUARY, 1))

    helper.migrations = migrations

    lastFacilityPullToken.set(None)
    lastPatientPullToken.set(None)
  }

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

    val db_v7 = helper.migrateTo(7)

    val cursor = db_v7.query("SELECT * FROM `LoggedInUserFacilityMapping`")
    cursor.use {
      assertThat(cursor.count).isEqualTo(1)

      it.moveToFirst()
      assertThat(it.string("userUuid")).isEqualTo("c6834f82-3305-4144-9dc8-5f77c908ebf1")
      assertThat(it.string("facilityUuid")).isEqualTo("43dad34c-139e-4e5f-976e-a3ef1d9ac977")
      assertThat(it.boolean("isCurrentFacility")).isTrue()
    }

    db_v7.query("SELECT * FROM LoggedInUser").use {
      assertThat(it.columnNames.contains("facilityUuid")).isFalse()
    }
  }

  @Test
  fun migration_6_to_7_without_an_existing_user() {
    helper.createDatabase(version = 6)
    val db_v7 = helper.migrateTo(7)

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

    val db_v8 = helper.migrateTo(8)

    val cursor = db_v8.query("""SELECT "loggedInStatus" FROM "LoggedInUser" WHERE "uuid"='c6834f82-3305-4144-9dc8-5f77c908ebf1'""")

    cursor.use {
      assertThat(it.moveToFirst()).isTrue()
      assertThat(it.string("loggedInStatus")).isEqualTo("LOGGED_IN")
    }
  }

  @Test
  fun migration_8_to_9() {
    helper.createDatabase(version = 8)
    val db_v9 = helper.migrateTo(9)

    db_v9.query("SELECT * FROM `Appointment`").use {
      assertThat(it.columnCount).isEqualTo(9)
    }
  }

  @Test
  fun migration_9_to_10() {
    helper.createDatabase(version = 9)
    val db_v10 = helper.migrateTo(10)

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

    val db_v11 = helper.migrateTo(11)

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
    val db_v12 = helper.migrateTo(12)

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

    val db_v13 = helper.migrateTo(13)

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

    val db_v14 = helper.migrateTo(14)

    db_v14.query("SELECT * FROM `MedicalHistory` WHERE patientUuid = '$patientUuid'").use {
      assertThat(it.count).isEqualTo(1)
      it.moveToFirst()

      val falseAsInt = 0
      assertThat(it.string("uuid")).isNotEqualTo("old-uuid")
      assertThat(it.integer("hasHadHeartAttack")).isEqualTo(falseAsInt)
      assertThat(it.integer("hasHadStroke")).isEqualTo(falseAsInt)
      assertThat(it.integer("hasHadKidneyDisease")).isEqualTo(falseAsInt)
      assertThat(it.integer("isOnTreatmentForHypertension")).isEqualTo(falseAsInt)
      assertThat(it.integer("hasDiabetes")).isEqualTo(falseAsInt)
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

    val db_v15 = helper.migrateTo(15)
    db_v15.query("SELECT * FROM `MedicalHistory`").use {
      assertThat(it.columnCount).isEqualTo(11)

      it.moveToFirst()
      assertThat(it.string("uuid")).isEqualTo("464bcda8-b26a-484d-bb70-49b3675f4a38")
      assertThat(it.string("diagnosedWithHypertension")).isEqualTo("0")
    }
  }

  @Test
  fun migration_15_to_16() {
    val db_v15 = helper.createDatabase(version = 15)

    // We need to do this here because Room does not create virtual tables for us.
    db_v15.execSQL("""CREATE VIRTUAL TABLE "PatientFuzzySearch" USING spellfix1""")

    db_v15.assertTableExists("PatientFuzzySearch")

    val db_v16 = helper.migrateTo(16)

    db_v16.assertTableDoesNotExist("PatientFuzzySearch")
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

    val db_17 = helper.migrateTo(17)

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

  @Test
  fun migration_17_to_18() {
    val db_v17 = helper.createDatabase(version = 17)

    val nonExistentFacilityUuid = "non-existent-facility-uuid"
    val existentFacilityUuid = "464bcda8-b26a-484d-bb70-49b3675f4a38"

    val measurementUuid1 = "ee367a66-f47e-42d8-965b-7a2b5c54f4bd"
    val prescribedDrugUuid1 = "814132f2-f440-40cb-991c-43861c295815"

    val measurementUuid2 = "fe367a66-f47e-42d8-965b-7a2b5c54f4bd"
    val prescribedDrugUuid2 = "124132f2-f440-40cb-991c-43861c295815"

    val userUuid = "a1d33096-cea6-4beb-8441-82cab2befe2d"
    val patientUuid = "464bcda8-b26a-484d-bb70-59b3675f4a38"

    fun insertFacilityStatement(uuid: String, name: String) = """
      INSERT INTO "Facility" VALUES (
        '$uuid',
        '$name',
        'Facility type',
        'Street address',
        'Village or colony',
        'District',
        'State',
        'Country',
        'Pin code',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'PENDING')
    """

    fun insertBloodPressureStatement(uuid: String, facilityUuid: String) = """
      INSERT INTO "BloodPressureMeasurement" VALUES(
        '$uuid',
        120,
        110,
        'IN_FLIGHT',
        '$userUuid',
        '$facilityUuid',
        '$patientUuid',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """

    fun insertPrescribedDrugStatement(uuid: String, facilityUuid: String) = """
      INSERT INTO "PrescribedDrug" VALUES(
        '$uuid',
        'Drug name',
        'Dosage',
        'rxNormCode',
        0,
        1,
        '$patientUuid',
        '$facilityUuid',
        'PENDING',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """

    db_v17.execSQL(insertFacilityStatement(uuid = existentFacilityUuid, name = "Facility name"))
    db_v17.execSQL(insertBloodPressureStatement(uuid = measurementUuid1, facilityUuid = existentFacilityUuid))
    db_v17.execSQL(insertPrescribedDrugStatement(uuid = prescribedDrugUuid1, facilityUuid = existentFacilityUuid))

    var thrownException: Throwable? = null
    try {
      db_v17.execSQL(insertBloodPressureStatement(uuid = measurementUuid2, facilityUuid = nonExistentFacilityUuid))
      db_v17.execSQL(insertPrescribedDrugStatement(uuid = prescribedDrugUuid2, facilityUuid = nonExistentFacilityUuid))

    } catch (e: Throwable) {
      thrownException = e
    }
    assertThat(thrownException).isNotNull()
    assertThat(thrownException).isInstanceOf(SQLiteConstraintException::class.java)

    val db_v18 = helper.migrateTo(18)

    db_v18.execSQL("""DELETE FROM "Facility" WHERE "uuid" = '$existentFacilityUuid'""")

    db_v18.query("""SELECT * FROM "BloodPressureMeasurement"""").use {
      it.moveToNext()
      assertThat(it.count).isEqualTo(1)
      assertThat(it.columnCount).isEqualTo(9)
      assertThat(it.string("uuid")).isEqualTo(measurementUuid1)
    }

    db_v18.query("""SELECT * FROM "PrescribedDrug"""").use {
      it.moveToNext()
      assertThat(it.count).isEqualTo(1)
      assertThat(it.columnCount).isEqualTo(11)
      assertThat(it.string("uuid")).isEqualTo(prescribedDrugUuid1)
    }

    // Inserting records for a non-existent facility should now not fail.
    db_v18.execSQL(insertBloodPressureStatement(uuid = measurementUuid2, facilityUuid = nonExistentFacilityUuid))
    db_v18.execSQL(insertPrescribedDrugStatement(uuid = prescribedDrugUuid2, facilityUuid = nonExistentFacilityUuid))
  }

  @Test
  fun migration_18_to_19() {
    helper.createDatabase(version = 18)
    val db_v19 = helper.migrateTo(19)

    db_v19.query("""SELECT * FROM "OngoingLoginEntry" """).use {
      assertThat(it.columnCount).isEqualTo(3)
    }
  }

  @Test
  fun migration_19_to_20() {
    val db_19 = helper.createDatabase(version = 19)
    val historyUuid = "464bcda8-b26a-484d-bb70-49b3675f4a38"

    db_19.execSQL("""
      INSERT OR REPLACE INTO "MedicalHistory" VALUES(
        '$historyUuid',
        'ee367a66-f47e-42d8-965b-7a2b5c54f4bd',
        0,
        1,
        0,
        1,
        0,
        1,
        'IN_FLIGHT',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """)

    val db_20 = helper.migrateTo(20)

    db_20.query("""SELECT * FROM "MedicalHistory" ORDER BY "createdAt" DESC""").use {
      assertThat(it.count).isEqualTo(1)

      it.moveToFirst()
      assertThat(it.string("diagnosedWithHypertension")).isEqualTo("NO")
      assertThat(it.string("isOnTreatmentForHypertension")).isEqualTo("YES")
      assertThat(it.string("hasHadHeartAttack")).isEqualTo("NO")
      assertThat(it.string("hasHadStroke")).isEqualTo("YES")
      assertThat(it.string("hasHadKidneyDisease")).isEqualTo("NO")
      assertThat(it.string("hasDiabetes")).isEqualTo("YES")
    }
  }

  @Test
  fun migration_20_to_21() {
    val db_20 = helper.createDatabase(version = 20)

    val patientUuid = UUID.randomUUID()

    val insertAppointmentWithCancelReason = { reason: String ->
      db_20.execSQL("""
      INSERT OR REPLACE INTO "Appointment" VALUES (
        '${UUID.randomUUID()}',
        '$patientUuid',
        'facility-uuid',
        'scheduled-date',
        'status',
        '$reason',
        'remind-on',
        1,
        'sync-status',
        'created-at',
        'updated-at'
      )
    """)
    }

    db_20.inTransaction {
      insertAppointmentWithCancelReason("PATIENT_NOT_RESPONDING")
      insertAppointmentWithCancelReason("MOVED")
      insertAppointmentWithCancelReason("DEAD")
      insertAppointmentWithCancelReason("OTHER")
    }

    val db_21 = helper.migrateTo(21)

    db_21.query("""SELECT * FROM "Appointment" WHERE "patientUuid" = '$patientUuid'""").use {
      assertThat(it.count).isEqualTo(4)
    }
    db_21.query("""SELECT * FROM "Appointment" WHERE "cancelReason" = 'not_responding'""").use {
      assertThat(it.count).isEqualTo(1)
    }
    db_21.query("""SELECT * FROM "Appointment" WHERE "cancelReason" = 'moved'""").use {
      assertThat(it.count).isEqualTo(1)
    }
    db_21.query("""SELECT * FROM "Appointment" WHERE "cancelReason" = 'dead'""").use {
      assertThat(it.count).isEqualTo(1)
    }
    db_21.query("""SELECT * FROM "Appointment" WHERE "cancelReason" = 'other'""").use {
      assertThat(it.count).isEqualTo(1)
    }
  }

  @Test
  fun migration_21_to_22() {
    helper.createDatabase(version = 21)
    val db_22 = helper.migrateTo(22)

    db_22.query("""SELECT * FROM "Protocol" """).use {
      assertThat(it.columnCount).isEqualTo(6)
    }

    db_22.query("""SELECT * FROM "ProtocolDrug" """).use {
      assertThat(it.columnCount).isEqualTo(8)
    }
  }

  @Test
  fun migration_22_to_23() {
    val db_22 = helper.createDatabase(version = 22)
    val protocolUuid = UUID.randomUUID()

    db_22.execSQL("""
      INSERT INTO "Protocol" VALUES(
      '$protocolUuid',
      'protocol-1',
      '0',
      'created-at',
      'updated-at',
      'PENDING')
    """)

    db_22.execSQL("""
      INSERT INTO "ProtocolDrug" VALUES(
      '${UUID.randomUUID()}',
      '$protocolUuid',
      'amlodipine',
      'rxnorm-code-1',
      '20mg',
      'created-at',
      'updated-at',
      'PENDING')
    """)

    db_22.query("""
      SELECT * FROM "ProtocolDrug"
    """).use {
      assertThat(it.columnCount).isEqualTo(8)
    }

    val db_23 = helper.migrateTo(23)
    db_23.query("""
      SELECT * FROM "ProtocolDrug"
    """).use {
      it.moveToFirst()
      assertThat(it.string("protocolUuid")).isEqualTo(protocolUuid.toString())
      assertThat(it.columnCount).isEqualTo(7)
    }
  }

  @Test
  fun migration_23_to_24() {
    val db_v23 = helper.createDatabase(version = 23)

    db_v23.apply {
      assertColumnCount(tableName = "BloodPressureMeasurement", expectedCount = 9)
      assertColumnCount(tableName = "PrescribedDrug", expectedCount = 11)
      assertColumnCount(tableName = "Facility", expectedCount = 12)
      assertColumnCount(tableName = "MedicalHistory", expectedCount = 11)
      assertColumnCount(tableName = "Appointment", expectedCount = 11)
      assertColumnCount(tableName = "Communication", expectedCount = 8)
      assertColumnCount(tableName = "Patient", expectedCount = 13)
      assertColumnCount(tableName = "PatientAddress", expectedCount = 7)
      assertColumnCount(tableName = "PatientPhoneNumber", expectedCount = 7)
      assertColumnCount(tableName = "Protocol", expectedCount = 6)
      assertColumnCount(tableName = "ProtocolDrug", expectedCount = 7)
    }

    val db_v24 = helper.migrateTo(24)

    db_v24.apply {
      assertColumnCount(tableName = "BloodPressureMeasurement", expectedCount = 10)
      assertColumnCount(tableName = "PrescribedDrug", expectedCount = 12)
      assertColumnCount(tableName = "Facility", expectedCount = 13)
      assertColumnCount(tableName = "MedicalHistory", expectedCount = 12)
      assertColumnCount(tableName = "Appointment", expectedCount = 12)
      assertColumnCount(tableName = "Communication", expectedCount = 9)
      assertColumnCount(tableName = "Patient", expectedCount = 14)
      assertColumnCount(tableName = "PatientAddress", expectedCount = 8)
      assertColumnCount(tableName = "PatientPhoneNumber", expectedCount = 8)
      assertColumnCount(tableName = "Protocol", expectedCount = 7)
      assertColumnCount(tableName = "ProtocolDrug", expectedCount = 8)
    }
  }

  @Test
  fun migration_24_to_25() {
    val db_v24 = helper.createDatabase(version = 24)
    db_v24.assertColumnCount(tableName = "Facility", expectedCount = 13)

    db_v24.execSQL("""
      INSERT INTO "Facility" VALUES (
        'facility-uuid',
        'facility-name',
        'Facility type',
        'Street address',
        'Village or colony',
        'District',
        'State',
        'Country',
        'Pin code',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'PENDING',
        '2018-09-25T11:20:42.008Z')
    """)

    val db_v25 = helper.migrateTo(25)
    db_v25.query("""SELECT * FROM "Facility"""").use {
      it.moveToNext()
      assertThat(it.string("protocolUuid")).isNull()
      assertThat(it.columnCount).isEqualTo(14)
    }
  }

  @Test
  fun migration_25_to_26() {
    val db_v25 = helper.createDatabase(version = 25)
    val protocolUuid = UUID.randomUUID()
    db_v25.execSQL("""
      INSERT INTO "Protocol" VALUES(
      '$protocolUuid',
      'protocol-1',
      '0',
      'created-at',
      'updated-at',
      'PENDING',
      'null')
    """)
    db_v25.execSQL("""
      INSERT INTO "ProtocolDrug" VALUES (
      'protocolDrug-uuid',
      '$protocolUuid',
      'Amlodipine',
      'rx-norm',
      '20mg',
      '2018-09-25T11:20:42.008Z',
      '2018-09-25T11:20:42.008Z',
      'null')
    """)

    db_v25.assertColumnCount(tableName = "ProtocolDrug", expectedCount = 8)

    val db_26 = helper.migrateTo(26)
    db_26.query("""
      SELECT * FROM "ProtocolDrug"
    """).use {
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(9)
      assertThat(it.integer("order")).isEqualTo(0)
      assertThat(it.string("protocolUuid")).isEqualTo(protocolUuid.toString())
    }
  }

  @Test
  fun migration_26_to_27() {
    val db_v26 = helper.createDatabase(version = 26)
    db_v26.assertColumnCount(tableName = "Facility", expectedCount = 14)

    db_v26.execSQL("""
      INSERT INTO "Facility" VALUES (
        'facility-uuid',
        'facility-name',
        'Facility type',
        'Street address',
        'Village or colony',
        'District',
        'State',
        'Country',
        'Pin code',
        'protocol-uuid',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'PENDING',
        '2018-09-25T11:20:42.008Z')
    """)

    val db_v27 = helper.migrateTo(27)
    db_v27.query("""SELECT * FROM "Facility"""").use {
      it.moveToNext()
      assertThat(it.columnNames.contains("groupUuid"))
      assertThat(it.string("groupUuid")).isNull()
      assertThat(it.columnCount).isEqualTo(15)
    }
  }

  @Test
  fun migration_27_to_28() {
    val db_v27 = helper.createDatabase(version = 27)
    db_v27.assertColumnCount(tableName = "Facility", expectedCount = 15)

    db_v27.execSQL("""
      INSERT INTO "Facility" VALUES (
        'facility-uuid',
        'facility-name',
        'facility-type',
        'street-address',
        'village-or-colony',
        'district',
        'state',
        'country',
        'pin code',
        'protocol-uuid',
        'group-uuid',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'PENDING',
        '2018-09-25T11:20:42.008Z')
    """)

    lastFacilityPullToken.set(Just("old-token"))

    val db_v28 = helper.migrateTo(28)
    db_v28.query("""SELECT * FROM Facility""").use {
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(17)

      assertThat(it.string("uuid")).isEqualTo("facility-uuid")
      assertThat(it.string("name")).isEqualTo("facility-name")
      assertThat(it.string("facilityType")).isEqualTo("facility-type")
      assertThat(it.string("streetAddress")).isEqualTo("street-address")
      assertThat(it.string("villageOrColony")).isEqualTo("village-or-colony")
      assertThat(it.string("district")).isEqualTo("district")
      assertThat(it.string("state")).isEqualTo("state")
      assertThat(it.string("country")).isEqualTo("country")
      assertThat(it.string("pinCode")).isEqualTo("pin code")
      assertThat(it.string("protocolUuid")).isEqualTo("protocol-uuid")
      assertThat(it.string("groupUuid")).isEqualTo("group-uuid")
      assertThat(it.string("location_latitude")).isNull()
      assertThat(it.string("location_longitude")).isNull()
      assertThat(it.string("createdAt")).isEqualTo("2018-09-25T11:20:42.008Z")
      assertThat(it.string("updatedAt")).isEqualTo("2018-09-25T11:20:42.008Z")
      assertThat(it.string("syncStatus")).isEqualTo("PENDING")
      assertThat(it.string("deletedAt")).isEqualTo("2018-09-25T11:20:42.008Z")
    }

    assertThat(lastFacilityPullToken.get()).isEqualTo(None)
  }

  @Test
  fun migration_28_to_29_verify_syncStatus_updated_for_Patient() {
    val tableName = "Patient"
    val columnCount = 14

    val db_v28 = helper.createDatabase(version = 28)
    db_v28.assertColumnCount(tableName = tableName, expectedCount = columnCount)

    val addressUuid = "ddb15d83-f390-4f6b-96c5-b2f5064cae6d"

    db_v28.execSQL("""
      INSERT INTO "PatientAddress" VALUES(
        '$addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """)

    db_v28.execSQL("""
      INSERT INTO "Patient" VALUES(
        'patientUuid',
        '$addressUuid',
        'Ash Kumari',
        'AshokKumar',
        'MALE',
        NULL,
        'ACTIVE',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'IN_FLIGHT',
        25,
        '2018-09-25T11:20:42.008Z',
        '1995-09-25');
    """)

    db_v28.execSQL("""
      INSERT INTO "Patient" VALUES(
        'patientUuid2',
        '$addressUuid',
        'Ash Kumari',
        'AshokKumar',
        'MALE',
        NULL,
        'ACTIVE',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'DONE',
        25,
        '2018-09-25T11:20:42.008Z',
        '1995-09-25');
    """)

    val db_v29 = helper.migrateTo(29)
    db_v29.query("""SELECT * FROM $tableName""").use {
      assertThat(it.columnCount).isEqualTo(columnCount)

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("PENDING")

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("DONE")
    }
  }

  @Test
  fun migration_28_to_29_verify_syncStatus_updated_for_BloodPressureMeasurement() {
    val db_v28 = helper.createDatabase(version = 28)
    val tableName = "BloodPressureMeasurement"
    db_v28.assertColumnCount(tableName = tableName, expectedCount = 10)

    db_v28.execSQL("""
      INSERT INTO "$tableName" VALUES(
        'uuid',
        120,
        110,
        'IN_FLIGHT',
        'userUuid',
        'facilityUuid',
        'patientUuid',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """)

    db_v28.execSQL("""
      INSERT INTO "$tableName" VALUES(
        'uuid2',
        120,
        110,
        'DONE',
        'userUuid',
        'facilityUuid',
        'patientUuid',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """)

    val db_v29 = helper.migrateTo(29)
    db_v29.query("""SELECT * FROM $tableName""").use {
      assertThat(it.columnCount).isEqualTo(10)

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("PENDING")

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("DONE")
    }
  }

  @Test
  fun migration_28_to_29_verify_syncStatus_updated_for_PrescribedDrug() {
    val db_v28 = helper.createDatabase(version = 28)
    val tableName = "PrescribedDrug"
    db_v28.assertColumnCount(tableName = tableName, expectedCount = 12)

    db_v28.execSQL("""
      INSERT INTO "PrescribedDrug" VALUES(
        'uuid',
        'Drug name',
        'Dosage',
        'rxNormCode',
        0,
        1,
        'patientUuid',
        'facilityUuid',
        'IN_FLIGHT',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """)

    db_v28.execSQL("""
      INSERT INTO "PrescribedDrug" VALUES(
        'uuid2',
        'Drug name',
        'Dosage',
        'rxNormCode',
        0,
        1,
        'patientUuid',
        'facilityUuid',
        'DONE',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z')
    """)

    val db_v29 = helper.migrateTo(29)
    db_v29.query("""SELECT * FROM $tableName""").use {
      assertThat(it.columnCount).isEqualTo(12)

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("PENDING")

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("DONE")
    }
  }

  @Test
  fun migration_28_to_29_verify_syncStatus_updated_for_Facility() {
    val db_v28 = helper.createDatabase(version = 28)
    db_v28.assertColumnCount(tableName = "Facility", expectedCount = 17)

    db_v28.execSQL("""
      INSERT INTO "Facility" VALUES (
        'facility-uuid',
        'facility-name',
        'facility-type',
        'street-address',
        'village-or-colony',
        'district',
        'state',
        'country',
        'pin code',
        'protocol-uuid',
        'group-uuid',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'IN_FLIGHT',
        '2018-09-25T11:20:42.008Z',
        0.0,
        1.0)
    """)

    db_v28.execSQL("""
      INSERT INTO "Facility" VALUES (
        'facility-uuid2',
        'facility-name',
        'facility-type',
        'street-address',
        'village-or-colony',
        'district',
        'state',
        'country',
        'pin code',
        'protocol-uuid',
        'group-uuid',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'DONE',
        '2018-09-25T11:20:42.008Z',
        0.0,
        1.0)
    """)

    val db_v29 = helper.migrateTo(29)
    db_v29.query("""SELECT * FROM Facility""").use {
      assertThat(it.columnCount).isEqualTo(17)

      it.moveToNext()

      assertThat(it.string("uuid")).isEqualTo("facility-uuid")
      assertThat(it.string("name")).isEqualTo("facility-name")
      assertThat(it.string("facilityType")).isEqualTo("facility-type")
      assertThat(it.string("streetAddress")).isEqualTo("street-address")
      assertThat(it.string("villageOrColony")).isEqualTo("village-or-colony")
      assertThat(it.string("district")).isEqualTo("district")
      assertThat(it.string("state")).isEqualTo("state")
      assertThat(it.string("country")).isEqualTo("country")
      assertThat(it.string("pinCode")).isEqualTo("pin code")
      assertThat(it.string("protocolUuid")).isEqualTo("protocol-uuid")
      assertThat(it.string("groupUuid")).isEqualTo("group-uuid")
      assertThat(it.string("createdAt")).isEqualTo("2018-09-25T11:20:42.008Z")
      assertThat(it.string("updatedAt")).isEqualTo("2018-09-25T11:20:42.008Z")
      assertThat(it.string("syncStatus")).isEqualTo("PENDING")
      assertThat(it.string("deletedAt")).isEqualTo("2018-09-25T11:20:42.008Z")
      assertThat(it.double("location_latitude")).isEqualTo(0.0)
      assertThat(it.double("location_longitude")).isEqualTo(1.0)

      it.moveToNext()

      assertThat(it.string("uuid")).isEqualTo("facility-uuid2")
      assertThat(it.string("name")).isEqualTo("facility-name")
      assertThat(it.string("facilityType")).isEqualTo("facility-type")
      assertThat(it.string("streetAddress")).isEqualTo("street-address")
      assertThat(it.string("villageOrColony")).isEqualTo("village-or-colony")
      assertThat(it.string("district")).isEqualTo("district")
      assertThat(it.string("state")).isEqualTo("state")
      assertThat(it.string("country")).isEqualTo("country")
      assertThat(it.string("pinCode")).isEqualTo("pin code")
      assertThat(it.string("protocolUuid")).isEqualTo("protocol-uuid")
      assertThat(it.string("groupUuid")).isEqualTo("group-uuid")
      assertThat(it.string("createdAt")).isEqualTo("2018-09-25T11:20:42.008Z")
      assertThat(it.string("updatedAt")).isEqualTo("2018-09-25T11:20:42.008Z")
      assertThat(it.string("syncStatus")).isEqualTo("DONE")
      assertThat(it.string("deletedAt")).isEqualTo("2018-09-25T11:20:42.008Z")
      assertThat(it.double("location_latitude")).isEqualTo(0.0)
      assertThat(it.double("location_longitude")).isEqualTo(1.0)
    }
  }

  @Test
  fun migration_28_to_29_verify_syncStatus_updated_for_Appointment() {
    val tableName = "Appointment"
    val columnCount = 12

    val db_v28 = helper.createDatabase(version = 28)
    db_v28.assertColumnCount(tableName = tableName, expectedCount = columnCount)

    db_v28.execSQL("""
      INSERT INTO "Appointment" VALUES (
        'uuid',
        'patientUuid',
        'facility-uuid',
        'scheduled-date',
        'status',
        'reason',
        'remind-on',
        1,
        'IN_FLIGHT',
        'created-at',
        'updated-at',
        'deleted-at'
      )
    """)

    db_v28.execSQL("""
      INSERT INTO "Appointment" VALUES (
        'uuid2',
        'patientUuid',
        'facility-uuid',
        'scheduled-date',
        'status',
        'reason',
        'remind-on',
        1,
        'DONE',
        'created-at',
        'updated-at',
        'deleted-at'
      )
    """)

    val db_v29 = helper.migrateTo(29)
    db_v29.query("""SELECT * FROM $tableName""").use {
      assertThat(it.columnCount).isEqualTo(columnCount)

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("PENDING")

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("DONE")
    }
  }

  @Test
  fun migration_28_to_29_verify_syncStatus_updated_for_Communication() {
    val tableName = "Communication"
    val columnCount = 9

    val db_v28 = helper.createDatabase(version = 28)
    db_v28.assertColumnCount(tableName = tableName, expectedCount = columnCount)

    db_v28.execSQL("""
      INSERT INTO "Communication" VALUES (
        'uuid',
        'c6834f82-3305-4144-9dc8-5f77c908ebf1',
        'c64f76b5-0d37-46e2-9426-554e4f809498',
        'MANUAL_CALL',
        'AGREED_TO_VISIT',
        'IN_FLIGHT',
        '2018-06-21T10:15:58.666Z',
        '2018-06-21T10:15:58.666Z',
        '2018-06-21T10:15:58.666Z');
    """)

    db_v28.execSQL("""
      INSERT INTO "Communication" VALUES (
        'uuid2',
        'c6834f82-3305-4144-9dc8-5f77c908ebf1',
        'c64f76b5-0d37-46e2-9426-554e4f809498',
        'MANUAL_CALL',
        'AGREED_TO_VISIT',
        'DONE',
        '2018-06-21T10:15:58.666Z',
        '2018-06-21T10:15:58.666Z',
        '2018-06-21T10:15:58.666Z');
    """)

    val db_v29 = helper.migrateTo(29)
    db_v29.query("""SELECT * FROM $tableName""").use {
      assertThat(it.columnCount).isEqualTo(columnCount)

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("PENDING")

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("DONE")
    }
  }

  @Test
  fun migration_28_to_29_verify_syncStatus_updated_for_MedicalHistory() {
    val tableName = "MedicalHistory"
    val columnCount = 12

    val db_v28 = helper.createDatabase(version = 28)
    db_v28.assertColumnCount(tableName = tableName, expectedCount = columnCount)

    db_v28.execSQL("""
      INSERT INTO "MedicalHistory" VALUES(
        'uuid',
        'patientUuid',
        'yes',
        'yes',
        'yes',
        'yes',
        'yes',
        'yes',
        'IN_FLIGHT',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z'
      )
    """)

    db_v28.execSQL("""
      INSERT INTO "MedicalHistory" VALUES(
        'uuid2',
        'patientUuid',
        'yes',
        'yes',
        'yes',
        'yes',
        'yes',
        'yes',
        'DONE',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z'
      )
    """)

    val db_v29 = helper.migrateTo(29)
    db_v29.query("""SELECT * FROM $tableName""").use {
      assertThat(it.columnCount).isEqualTo(columnCount)

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("PENDING")

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("DONE")
    }
  }

  @Test
  fun migration_28_to_29_verify_syncStatus_updated_for_Protocol() {
    val tableName = "Protocol"
    val columnCount = 7

    val db_v28 = helper.createDatabase(version = 28)
    db_v28.assertColumnCount(tableName = tableName, expectedCount = columnCount)

    db_v28.execSQL("""
      INSERT INTO "Protocol" VALUES(
      'protocolUuid',
      'protocol-1',
      '0',
      'created-at',
      'updated-at',
      'IN_FLIGHT',
      'null')
    """)

    db_v28.execSQL("""
      INSERT INTO "Protocol" VALUES(
      'protocolUuid2',
      'protocol-1',
      '0',
      'created-at',
      'updated-at',
      'DONE',
      'null')
    """)

    val db_v29 = helper.migrateTo(29)
    db_v29.query("""SELECT * FROM $tableName""").use {
      assertThat(it.columnCount).isEqualTo(columnCount)

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("PENDING")

      it.moveToNext()

      assertThat(it.string("syncStatus")).isEqualTo("DONE")
    }
  }

  @Test
  fun migration_29_to_30() {
    val tableName = "BusinessId"
    val expectedColumnCount = 9

    lastPatientPullToken.set(Just("old_token"))
    val db_v29 = helper.createDatabase(29)
    db_v29.assertTableDoesNotExist(tableName)

    val db_v30 = helper.migrateTo(30)

    db_v30.assertColumnCount(tableName, expectedColumnCount)
    assertThat(lastPatientPullToken.get()).isEqualTo(None)
  }

  @Test
  fun migration_30_to_31() {
    val tableName = "MissingPhoneReminder"

    val db_v30 = helper.createDatabase(30)
    db_v30.assertTableDoesNotExist(tableName)

    val db_v31 = helper.migrateTo(31)
    db_v31.assertTableExists(tableName)
    db_v31.assertColumnCount(tableName, 2)
  }

  @Test
  fun migration_31_to_32() {
    val tableName = "Appointment"
    val columnCount_v31 = 12

    val db_31 = helper.createDatabase(version = 31)
    db_31.assertColumnCount(tableName, columnCount_v31)
    db_31.execSQL("""
      INSERT INTO "Appointment" VALUES(
        'uuid',
        'patientUuid',
        'facility-uuid',
        'scheduled-date',
        'status',
        'cancel-reason',
        'remind-on',
        1,
        'PENDING',
        'created-at',
        'updated-at',
        'deleted-at'
        )
    """)

    val db_32 = helper.migrateTo(version = 32)

    val columnCount_v32 = columnCount_v31 + 1

    db_32.query("""
      SELECT * FROM "Appointment"
    """).use {
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(columnCount_v32)

      assertThat(it.string("uuid")).isEqualTo("uuid")
      assertThat(it.string("patientUuid")).isEqualTo("patientUuid")
      assertThat(it.string("facilityUuid")).isEqualTo("facility-uuid")
      assertThat(it.string("scheduledDate")).isEqualTo("scheduled-date")
      assertThat(it.string("status")).isEqualTo("status")
      assertThat(it.string("cancelReason")).isEqualTo("cancel-reason")
      assertThat(it.string("remindOn")).isEqualTo("remind-on")
      assertThat(it.boolean("agreedToVisit")).isTrue()
      assertThat(it.string("isDefaulter")).isEqualTo(null)
      assertThat(it.string("syncStatus")).isEqualTo("PENDING")
      assertThat(it.string("createdAt")).isEqualTo("created-at")
      assertThat(it.string("updatedAt")).isEqualTo("updated-at")
      assertThat(it.string("deletedAt")).isEqualTo("deleted-at")
    }
  }

  @Test
  fun migration_32_to_33() {
    val tableName = "Appointment"
    val columnCount = 13

    val db_v32 = helper.createDatabase(version = 32)
    db_v32.assertColumnCount(tableName, columnCount)

    db_v32.execSQL("""
            INSERT INTO $tableName VALUES(
            'uuid',
            'patientUuid',
            'facility-uuid',
            'scheduled-date',
            'status',
            'cancel-reason',
            'remind-on',
            '1',
            null,
            'PENDING',
            'created-at',
            'updated-at',
            null)
        """)

    val db_v33 = helper.migrateTo(version = 33)

    db_v33.query("""
           SELECT * FROM $tableName
        """).use {
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(columnCount)

      assertThat(it.string("uuid")).isEqualTo("uuid")
      assertThat(it.string("patientUuid")).isEqualTo("patientUuid")
      assertThat(it.string("facilityUuid")).isEqualTo("facility-uuid")
      assertThat(it.string("scheduledDate")).isEqualTo("scheduled-date")
      assertThat(it.string("status")).isEqualTo("status")
      assertThat(it.string("cancelReason")).isEqualTo("cancel-reason")
      assertThat(it.string("remindOn")).isEqualTo("remind-on")
      assertThat(it.boolean("agreedToVisit")).isTrue()
      assertThat(it.string("appointmentType")).isEqualTo(null)
      assertThat(it.string("syncStatus")).isEqualTo("PENDING")
      assertThat(it.string("createdAt")).isEqualTo("created-at")
      assertThat(it.string("updatedAt")).isEqualTo("updated-at")
      assertThat(it.string("deletedAt")).isEqualTo(null)
    }
  }

  @Test
  fun migration_33_to_34() {
    val tableName = "Appointment"

    val db_v33 = helper.createDatabase(version = 33)

    db_v33.execSQL("""
            INSERT INTO $tableName VALUES(
            'uuid',
            'patientUuid',
            'facility-uuid',
            'scheduled-date',
            'status',
            'cancel-reason',
            'remind-on',
            '1',
            null,
            'PENDING',
            'created-at',
            'updated-at',
            null)
        """)

    val db_v34 = helper.migrateTo(version = 34)

    db_v34.query("""
           SELECT * FROM $tableName
        """).use {
      it.moveToNext()

      assertThat(it.string("uuid")).isEqualTo("uuid")
      assertThat(it.string("patientUuid")).isEqualTo("patientUuid")
      assertThat(it.string("facilityUuid")).isEqualTo("facility-uuid")
      assertThat(it.string("scheduledDate")).isEqualTo("scheduled-date")
      assertThat(it.string("status")).isEqualTo("status")
      assertThat(it.string("cancelReason")).isEqualTo("cancel-reason")
      assertThat(it.string("remindOn")).isEqualTo("remind-on")
      assertThat(it.boolean("agreedToVisit")).isTrue()
      assertThat(it.string("appointmentType")).isEqualTo("manual")
      assertThat(it.string("syncStatus")).isEqualTo("PENDING")
      assertThat(it.string("createdAt")).isEqualTo("created-at")
      assertThat(it.string("updatedAt")).isEqualTo("updated-at")
      assertThat(it.string("deletedAt")).isEqualTo(null)
    }
  }

  @Test
  fun migrate_34_to_35_verify_column_count() {
    val db_v34 = helper.createDatabase(version = 34)

    db_v34.apply {
      assertColumnCount(tableName = "BloodPressureMeasurement", expectedCount = 10)
      assertColumnCount(tableName = "PrescribedDrug", expectedCount = 12)
      assertColumnCount(tableName = "MedicalHistory", expectedCount = 12)
      assertColumnCount(tableName = "Appointment", expectedCount = 13)
      assertColumnCount(tableName = "Patient", expectedCount = 14)
      assertColumnCount(tableName = "PatientAddress", expectedCount = 8)
      assertColumnCount(tableName = "PatientPhoneNumber", expectedCount = 8)
      assertColumnCount(tableName = "BusinessId", expectedCount = 9)
    }

    val db_v35 = helper.migrateTo(version = 35)

    db_v35.apply {
      assertColumnCount(tableName = "BloodPressureMeasurement", expectedCount = 11)
      assertColumnCount(tableName = "PrescribedDrug", expectedCount = 13)
      assertColumnCount(tableName = "MedicalHistory", expectedCount = 13)
      assertColumnCount(tableName = "Appointment", expectedCount = 14)
      assertColumnCount(tableName = "Patient", expectedCount = 15)
      assertColumnCount(tableName = "PatientAddress", expectedCount = 9)
      assertColumnCount(tableName = "PatientPhoneNumber", expectedCount = 9)
      assertColumnCount(tableName = "BusinessId", expectedCount = 10)
    }
  }

  @Test
  fun migrate_34_to_35_verify_default_values() {
    val db_v34 = helper.createDatabase(version = 34)

    val bpTable = "BloodPressureMeasurement"
    val drugTable = "PrescribedDrug"
    val medicalHistoryTable = "MedicalHistory"
    val appointmentTable = "Appointment"
    val patientTable = "Patient"
    val patientAddressTable = "PatientAddress"
    val patientPhoneTable = "PatientPhoneNumber"
    val businessIdTable = "BusinessId"

    db_v34.execSQL("""
      INSERT INTO $bpTable VALUES(
      'uuid',
      120,
      90,
      'DONE',
      'user-uuid',
      'facility-uuid',
      'patient-Uuid',
      'created-at',
      'update-at',
      'null')
    """)

    db_v34.execSQL("""
      INSERT INTO $drugTable VALUES(
      'uuid',
      'drug',
      'dosage',
      'rxNormCode',
      0,
      1,
      'patientUuid',
      'facilityUuid',
      'PENDING',
      'created-at',
      'updatedAt',
      'null')
    """)

    db_v34.execSQL("""
      INSERT INTO $medicalHistoryTable VALUES(
      'uuid',
      'patientUuid',
      0,
      1,
      0,
      1,
      0,
      1,
      'PENDING',
      'created-at',
      'updated-at',
      'null')
    """)

    db_v34.execSQL("""
      INSERT INTO $appointmentTable VALUES(
      'uuid',
      'patientUuid',
      'facility-uuid',
      'scheduled-date',
      'status',
      'cancel-reason',
      'remind-on',
      '1',
      'manual',
      'PENDING',
      'created-at',
      'updated-at',
      'null')
    """)

    db_v34.execSQL("""
      INSERT INTO $patientAddressTable VALUES(
        'addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        'created-at',
        'updated-at',
        NULL)
    """)

    db_v34.execSQL("""
      INSERT INTO $patientTable VALUES(
        'patientUuid',
        'addressUuid',
        'AshokKumar',
        'AshokKumar',
        'MALE',
        NULL,
        'ACTIVE',
        'created-at',
        'updated-at',
        'null',
        'IN_FLIGHT',
        25,
        '2018-09-25T11:20:42.008Z',
        '1995-09-25');
    """)

    db_v34.execSQL("""
      INSERT INTO $patientPhoneTable VALUES(
      'uuid',
      'patientUuid',
      '981615191',
      'mobile',
      0,
      'created-at',
      'updated-at',
      'null')
    """)

    db_v34.execSQL("""
      INSERT INTO $businessIdTable VALUES(
      'uuid',
      'patientUuid',
      'meta-version',
      'data',
      'created-at',
      'updated-at',
      'null',
      'simple-uuid',
      'bp-passport'
      )
    """)

    val db_v35 = helper.migrateTo(version = 35)

    val tablesToVerifyRecordedAt = listOf(
        bpTable,
        drugTable,
        medicalHistoryTable,
        appointmentTable,
        patientAddressTable,
        patientPhoneTable,
        patientTable,
        businessIdTable
    )

    val newColumnName = "recordedAt"

    tablesToVerifyRecordedAt.forEach { tableName ->
      db_v35.query("""
      SELECT $newColumnName FROM $tableName
    """).use {
        it.moveToNext()
        assertThat(it.string(newColumnName)).isEqualTo("created-at")
      }
    }
  }

  @Test
  fun migrate_34_to_35_verify_default_recorded_at_for_patient() {
    val db_v34 = helper.createDatabase(version = 34)

    val bpTable = "BloodPressureMeasurement"
    val patientTable = "Patient"
    val patientAddressTable = "PatientAddress"

    db_v34.execSQL("""
      INSERT INTO $patientAddressTable VALUES(
        'addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        'created-at',
        'updated-at',
        NULL)
    """)

    db_v34.execSQL("""
      INSERT INTO $patientTable VALUES(
        'Patient created before first BP',
        'addressUuid',
        'AshokKumar',
        'AshokKumar',
        'MALE',
        NULL,
        'ACTIVE',
        '2018-09-23T11:20:42.008Z',
        'updated-at',
        'null',
        'DONE',
        25,
        'age-updated-at',
        'dob');
    """)

    db_v34.execSQL("""
      INSERT INTO $bpTable VALUES(
      'uuid1',
      120,
      90,
      'PENDING',
      'user-uuid',
      'facility-uuid',
      'Patient created before first BP',
      '2018-09-25T11:20:42.008Z',
      'update-at',
      NULL)
    """)

    db_v34.execSQL("""
      INSERT INTO $patientTable VALUES(
        'Patient registered after first BP was recorded',
        'addressUuid',
        'AlokKumar',
        'AKumar',
        'MALE',
        NULL,
        'ACTIVE',
        '2018-11-23T11:20:42.008Z',
        'updated-at',
        NULL,
        'DONE',
        25,
        'age-updated-at',
        'dob');
    """)

    db_v34.execSQL("""
      INSERT INTO $bpTable VALUES(
      'uuid2',
      120,
      90,
      'PENDING',
      'user-uuid',
      'facility-uuid',
      'Patient registered after first BP was recorded',
      '2018-09-25T11:20:42.008Z',
      'update-at',
      NULL)
    """)

    db_v34.execSQL("""
      INSERT INTO $patientTable VALUES(
        'No BP for patient',
        'addressUuid',
        'VijayaKumari',
        'VKumari',
        'FEMALE',
        NULL,
        'ACTIVE',
        '2018-11-23T11:20:42.008Z',
        'updated-at',
        NULL,
        'IN_FLIGHT',
        25,
        'age-updated-at',
        'dob');
    """)

    db_v34.execSQL("""
      INSERT INTO $patientTable VALUES(
        'Patient has deleted BPs only',
        'addressUuid',
        'RamaDevi',
        'RDevi',
        'FEMALE',
        NULL,
        'ACTIVE',
        '2018-11-23T11:20:42.008Z',
        'updated-at',
        NULL,
        'DONE',
        25,
        'age-updated-at',
        'dob');
    """)

    db_v34.execSQL("""
      INSERT INTO $bpTable VALUES(
      'uuid3',
      120,
      90,
      'PENDING',
      'user-uuid',
      'facility-uuid',
      'Patient has deleted BPs only',
      '2018-09-25T11:20:42.008Z',
      'update-at',
      'deleted-at')
    """)

    db_v34.execSQL("""
      INSERT INTO $patientTable VALUES(
        'Patient with multiple BPs',
        'addressUuid',
        'RamDev',
        'RDev',
        'MALE',
        NULL,
        'ACTIVE',
        '2018-11-23T11:20:42.008Z',
        'updated-at',
        NULL,
        'IN_FLIGHT',
        25,
        'age-updated-at',
        'dob');
    """)

    db_v34.execSQL("""
      INSERT INTO $bpTable VALUES(
      'uuid4',
      120,
      90,
      'PENDING',
      'user-uuid',
      'facility-uuid',
      'Patient with multiple BPs',
      '2017-09-25T11:20:42.008Z',
      'update-at',
      NULL)
    """)

    db_v34.execSQL("""
      INSERT INTO $bpTable VALUES(
      'uuid5',
      120,
      90,
      'DONE',
      'user-uuid',
      'facility-uuid',
      'Patient with multiple BPs',
      '2018-09-11T11:20:42.008Z',
      'update-at',
      NULL)
    """)

    db_v34.execSQL("""
      INSERT INTO $patientTable VALUES(
        'Patient with no pending BPs',
        'addressUuid',
        'Dev',
        'Dev',
        'MALE',
        NULL,
        'ACTIVE',
        '2018-11-23T11:20:42.008Z',
        'updated-at',
        NULL,
        'DONE',
        25,
        'age-updated-at',
        'dob');
    """)

    db_v34.execSQL("""
      INSERT INTO $bpTable VALUES(
      'uuid6',
      120,
      90,
      'DONE',
      'user-uuid',
      'facility-uuid',
      'Patient with no pending BPs',
      '2017-09-25T11:20:42.008Z',
      'update-at',
      NULL)
    """)

    val db_v35 = helper.migrateTo(version = 35)

    db_v35.query("""
      SELECT * FROM $patientTable WHERE uuid = 'Patient created before first BP'
    """).use {
      it.moveToNext()
      assertThat(it.string("recordedAt")).isEqualTo("2018-09-23T11:20:42.008Z")
      assertThat(it.string("syncStatus")).isEqualTo("DONE")
      assertThat(it.string("updatedAt")).isEqualTo("updated-at")
    }

    db_v35.query("""
      SELECT * FROM $patientTable WHERE uuid = 'Patient registered after first BP was recorded'
    """).use {
      it.moveToNext()
      assertThat(it.string("recordedAt")).isEqualTo("2018-09-25T11:20:42.008Z")
      assertThat(it.string("syncStatus")).isEqualTo("PENDING")
      assertThat(it.string("updatedAt")).isEqualTo(Instant.now(clock).toString())
    }

    db_v35.query("""
      SELECT * FROM $patientTable WHERE uuid = 'No BP for patient'
    """).use {
      it.moveToNext()
      assertThat(it.string("recordedAt")).isEqualTo("2018-11-23T11:20:42.008Z")
      assertThat(it.string("syncStatus")).isEqualTo("IN_FLIGHT")
      assertThat(it.string("updatedAt")).isEqualTo("updated-at")
    }

    db_v35.query("""
      SELECT * FROM $patientTable WHERE uuid = 'Patient has deleted BPs only'
    """).use {
      it.moveToNext()
      assertThat(it.string("recordedAt")).isEqualTo("2018-11-23T11:20:42.008Z")
      assertThat(it.string("syncStatus")).isEqualTo("DONE")
      assertThat(it.string("updatedAt")).isEqualTo("updated-at")
    }

    db_v35.query("""
      SELECT * FROM $patientTable WHERE uuid = 'Patient with multiple BPs'
    """).use {
      it.moveToNext()
      assertThat(it.string("recordedAt")).isEqualTo("2017-09-25T11:20:42.008Z")
      assertThat(it.string("syncStatus")).isEqualTo("PENDING")
      assertThat(it.string("updatedAt")).isEqualTo(Instant.now(clock).toString())
    }

    db_v35.query("""
      SELECT * FROM $patientTable WHERE uuid = 'Patient with no pending BPs'
    """).use {
      it.moveToNext()
      assertThat(it.string("recordedAt")).isEqualTo("2017-09-25T11:20:42.008Z")
      assertThat(it.string("syncStatus")).isEqualTo("DONE")
      assertThat(it.string("updatedAt")).isEqualTo("updated-at")
    }
  }

  @Test
  fun migrate_prescription_from_35_to_36() {
    val db_v35 = helper.createDatabase(version = 35)

    val tableName = "PrescribedDrug"
    val uuid = "drug-uuid"
    db_v35.execSQL("""
      INSERT INTO $tableName VALUES(
      '$uuid',
      'drug',
      'dosage',
      'rxNormCode',
      0,
      1,
      'patientUuid',
      'facilityUuid',
      'PENDING',
      'created-at',
      'updatedAt',
      'null',
      'recorded-at')
    """)

    val db_v36 = helper.migrateTo(version = 36)

    db_v36.query("SELECT * FROM $tableName").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(12)
      assertThat(it.string("uuid")).isEqualTo(uuid)
    }
  }

  @Test
  fun migrate_appointment_from_35_to_36() {
    val db_v35 = helper.createDatabase(version = 35)

    val tableName = "Appointment"
    val appointmentUuid = "uuid"

    db_v35.execSQL("""
      INSERT INTO $tableName VALUES(
      '$appointmentUuid',
      'patientUuid',
      'facility-uuid',
      'scheduled-date',
      'status',
      'cancel-reason',
      'remind-on',
      '1',
      'manual',
      'PENDING',
      'created-at',
      'updated-at',
      'null',
      'recordedAt')
    """)

    val db_v36 = helper.migrateTo(version = 36)

    db_v36.query("SELECT * FROM $tableName").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(13)
      assertThat(it.string("uuid")).isEqualTo(appointmentUuid)
    }
  }

  @Test
  fun migrate_medical_histories_from_35_to_36() {
    val db_v35 = helper.createDatabase(version = 35)

    val tableName = "MedicalHistory"
    val mhUuid = "uuid"

    db_v35.execSQL("""
      INSERT INTO $tableName VALUES(
      '$mhUuid',
      'patientUuid',
      0,
      1,
      0,
      1,
      0,
      1,
      'PENDING',
      'created-at',
      'updated-at',
      'null',
      'recorded-at')
    """)

    val db_v36 = helper.migrateTo(version = 36)

    db_v36.query("SELECT * FROM $tableName").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(12)
      assertThat(it.string("uuid")).isEqualTo(mhUuid)
    }
  }

  @Test
  fun migrate_patient_address_from_35_to_36() {
    val db_v35 = helper.createDatabase(version = 35)

    val addressTableName = "PatientAddress"
    val patientTable = "Patient"
    val addressUuid = "uuid"

    db_v35.execSQL("""
      INSERT INTO $addressTableName VALUES(
        '$addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        'created-at',
        'updated-at',
        NULL,
        'recorded-at')
    """)

    db_v35.execSQL("""
      INSERT INTO $patientTable VALUES(
        'patientUuid',
        '$addressUuid',
        'patient-name',
        'name',
        'MALE',
        NULL,
        'ACTIVE',
        'created-at',
        'updated-at',
        'null',
        'IN_FLIGHT',
        'recorded-at',
        25,
        'age-updated-at',
        'dob');
    """)

    val db_v36 = helper.migrateTo(version = 36)

    db_v36.query("SELECT * FROM $addressTableName").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(8)
      assertThat(it.string("uuid")).isEqualTo(addressUuid)
    }

    db_v36.query("SELECT * FROM $patientTable").use {
      assertThat(it.count).isEqualTo(1)
      it.moveToNext()
      assertThat(it.string("addressUuid")).isEqualTo(addressUuid)
      assertThat(it.string("uuid")).isEqualTo("patientUuid")
    }
  }

  @Test
  fun migrate_phone_number_from_35_to_36() {
    val db_v35 = helper.createDatabase(version = 35)

    val phoneNumberTableName = "PatientPhoneNumber"
    val patientTable = "Patient"
    val patientUuid = "patientUuid"
    val phoneUuid = "phoneUuid"

    db_v35.execSQL("""
      INSERT INTO "PatientAddress" VALUES(
        'addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        'created-at',
        'updated-at',
        NULL,
        'recorded-at')
    """)

    db_v35.execSQL("""
      INSERT INTO $patientTable VALUES(
        '$patientUuid',
        'addressUuid',
        'patient-name',
        'name',
        'MALE',
        NULL,
        'ACTIVE',
        'created-at',
        'updated-at',
        'null',
        'IN_FLIGHT',
        'recorded-at',
        25,
        'age-updated-at',
        'dob');
    """)

    db_v35.execSQL("""
      INSERT INTO $phoneNumberTableName VALUES(
      '$phoneUuid',
      '$patientUuid',
      '981615191',
      'mobile',
      0,
      'created-at',
      'updated-at',
      'null',
      'recorded-at')
    """)

    val db_v36 = helper.migrateTo(version = 36)

    db_v36.query("SELECT * FROM $phoneNumberTableName").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(8)
      assertThat(it.string("patientUuid")).isEqualTo(patientUuid)
      assertThat(it.string("uuid")).isEqualTo(phoneUuid)
    }
  }

  @Test
  fun migrate_business_ids_from_35_to_36() {
    val db_v35 = helper.createDatabase(version = 35)

    val patientTable = "Patient"
    val businessIdTable = "BusinessId"
    val patientUuid = "patientUuid"
    val businessUuid = "businessUuid"

    db_v35.execSQL("""
      INSERT INTO "PatientAddress" VALUES(
        'addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        'created-at',
        'updated-at',
        NULL,
        'recorded-at')
    """)

    db_v35.execSQL("""
      INSERT INTO $patientTable VALUES(
        '$patientUuid',
        'addressUuid',
        'patient-name',
        'name',
        'MALE',
        NULL,
        'ACTIVE',
        'created-at',
        'updated-at',
        'null',
        'IN_FLIGHT',
        'recorded-at',
        25,
        'age-updated-at',
        'dob')
    """)

    db_v35.execSQL("""
      INSERT INTO $businessIdTable VALUES (
      '$businessUuid',
      'patientUuid',
      'meta-version',
      'data',
      'created-at',
      'updated-at',
      'null',
      'recorded-at',
      'simple-uuid',
      'bp-passport'
      )
    """)

    val db_v36 = helper.migrateTo(version = 36)

    db_v36.query("SELECT * FROM $businessIdTable").use {
      assertThat(it.count).isEqualTo(1)
      assertThat(it.getColumnIndex("recordedAt")).isEqualTo(-1)
      it.moveToNext()
      assertThat(it.columnCount).isEqualTo(9)
      assertThat(it.string("patientUuid")).isEqualTo(patientUuid)
      assertThat(it.string("uuid")).isEqualTo(businessUuid)
    }
  }

  @Test
  fun migrate_userstatus_from_36_to_37() {
    val db_v36 = helper.createDatabase(version = 36)

    fun insertUser(uuid: String, oldStatus: String) {
      db_v36.execSQL("""
        INSERT OR ABORT INTO LoggedInUser (uuid, fullName, phoneNumber, pinDigest,  status, createdAt, updatedAt, loggedInStatus)
        VALUES (
          '$uuid',
          'Ashok Kumar',
          '1234567890',
          'pinDigest',
          '$oldStatus',
          '2018-06-21T10:15:58.666Z',
          '2018-06-21T10:15:58.666Z',
          'NOT_LOGGED_IN'
        )
      """)
    }

    val uuid1 = UUID.randomUUID().toString()
    insertUser(uuid = uuid1, oldStatus = "WAITING_FOR_APPROVAL")

    val uuid2 = UUID.randomUUID().toString()
    insertUser(uuid = uuid2, oldStatus = "APPROVED_FOR_SYNCING")

    val uuid3 = UUID.randomUUID().toString()
    insertUser(uuid = uuid3, oldStatus = "DISAPPROVED_FOR_SYNCING")

    val db_v37 = helper.migrateTo(version = 37)

    fun verifyNewStatus(uuid: String, newStatus: String) {
      val cursor = db_v37.query("SELECT status FROM LoggedInUser WHERE uuid='$uuid'")

      cursor.use {
        assertThat(it.moveToFirst()).isTrue()
        assertThat(it.string("status")).isEqualTo(newStatus)
      }
    }

    verifyNewStatus(uuid = uuid1, newStatus = "requested")
    verifyNewStatus(uuid = uuid2, newStatus = "allowed")
    verifyNewStatus(uuid = uuid3, newStatus = "denied")
  }

  @Test
  fun migrate_appointment_status_from_37_to_38() {
    val db_v37 = helper.createDatabase(version = 37)

    fun insertAppointment(uuid: String, oldStatus: String) {
      db_v37.execSQL("""
        INSERT OR REPLACE INTO Appointment
          (uuid, patientUuid, facilityUuid, scheduledDate, status, appointmentType, syncStatus, createdAt, updatedAt)
        VALUES (
          '$uuid',
          'a1d33096-cea6-4beb-8441-82cab2befe2d',
          '0274a4a6-dd0e-493c-86aa-6502cd1fc2a0',
          '2011-12-03',
          '$oldStatus',
          'automatic',
          'PENDING',
          '2018-06-21T10:15:58.666Z',
          '2018-06-21T10:15:58.666Z'
        );
      """)
    }

    val uuid1 = UUID.randomUUID().toString()
    insertAppointment(uuid = uuid1, oldStatus = "SCHEDULED")

    val uuid2 = UUID.randomUUID().toString()
    insertAppointment(uuid = uuid2, oldStatus = "CANCELLED")

    val uuid3 = UUID.randomUUID().toString()
    insertAppointment(uuid = uuid3, oldStatus = "VISITED")

    val db_v38 = helper.migrateTo(version = 38)

    fun verifyNewAppointmentStatus(uuid: String, newStatus: String) {
      val cursor = db_v38.query("SELECT status FROM Appointment WHERE uuid='$uuid'")

      cursor.use {
        assertThat(it.moveToFirst()).isTrue()
        assertThat(it.string("status")).isEqualTo(newStatus)
      }
    }

    verifyNewAppointmentStatus(uuid = uuid1, newStatus = "scheduled")
    verifyNewAppointmentStatus(uuid = uuid2, newStatus = "cancelled")
    verifyNewAppointmentStatus(uuid = uuid3, newStatus = "visited")
  }

  @Test
  fun migrate_patient_status_from_38_to_39() {
    val db_38 = helper.createDatabase(version = 38)

    db_38.execSQL("""
      INSERT INTO PatientAddress VALUES(
        'addressUuid',
        'colony or village',
        'district',
        'state',
        'country',
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        NULL
      )
    """)

    fun insertPatient(oldStatus: String): UUID {
      val uuid = UUID.randomUUID()
      db_38.execSQL("""
        INSERT INTO Patient VALUES(
          '$uuid',
          'addressUuid',
          'Ash Kumar',
          'AshokKumar',
          'MALE',
          NULL,
          '$oldStatus',
          'created-at',
          'updated-at',
          NULL,
          'recorded-at',
          'DONE',
          23,
          'age-updated-at',
          'dob'
        )
      """)
      return uuid
    }

    val activePatient = insertPatient("ACTIVE")
    val inactivePatient = insertPatient("INACTIVE")
    val migratedPatient = insertPatient("MIGRATED")
    val deadPatient = insertPatient("DEAD")
    val unresponsivePatient = insertPatient("UNRESPONSIVE")

    val db_39 = helper.migrateTo(version = 39)

    fun verifyNewStatus(uuid: UUID, newStatus: String) {
      val cursor = db_39.query("""
        SELECT status FROM Patient WHERE uuid = '$uuid'
      """)

      assertThat(cursor.count).isEqualTo(1)
      cursor.use {
        it.moveToFirst()
        assertThat(it.string("status")).isEqualTo(newStatus)
      }
    }

    verifyNewStatus(activePatient, "active")
    verifyNewStatus(inactivePatient, "inactive")
    verifyNewStatus(migratedPatient, "migrated")
    verifyNewStatus(deadPatient, "dead")
    verifyNewStatus(unresponsivePatient, "unresponsive")
  }

  @Test
  fun migrate_medical_history_answer_from_39_to_40() {
    data class AnswerTuple(
        val medicalHistoryUuid: UUID,
        val diagnosedWithHypertension: String,
        val isOnTreatmentForHypertension: String,
        val hasHadHeartAttack: String,
        val hasHadStroke: String,
        val hasHadKidneyDisease: String,
        val hasDiabetes: String
    )

    fun saveMedicalHistory(db: SupportSQLiteDatabase, answerTuple: AnswerTuple) {
      db.execSQL("""
        INSERT INTO "MedicalHistory" (
            "uuid", "patientUuid", "syncStatus",
            "diagnosedWithHypertension", "isOnTreatmentForHypertension", "hasHadHeartAttack",
            "hasHadStroke", "hasHadKidneyDisease", "hasDiabetes",
            "createdAt", "updatedAt", "deletedAt"
        ) VALUES (
            '${answerTuple.medicalHistoryUuid}', 'patient-id', 'DONE',
            '${answerTuple.diagnosedWithHypertension}', '${answerTuple.isOnTreatmentForHypertension}', '${answerTuple.hasHadHeartAttack}',
            '${answerTuple.hasHadStroke}', '${answerTuple.hasHadKidneyDisease}', '${answerTuple.hasDiabetes}',
            'created_at', 'updated_at', NULL
        )
      """)
    }

    fun verifyMedicalHistory(db: SupportSQLiteDatabase, answerTuple: AnswerTuple) {
      db.query("""
        SELECT * FROM "MedicalHistory" WHERE "uuid" = '${answerTuple.medicalHistoryUuid}'
      """).use { cursor ->
        cursor.moveToNext()
        assertThat(cursor.string("diagnosedWithHypertension")).isEqualTo(answerTuple.diagnosedWithHypertension)
        assertThat(cursor.string("isOnTreatmentForHypertension")).isEqualTo(answerTuple.isOnTreatmentForHypertension)
        assertThat(cursor.string("hasHadHeartAttack")).isEqualTo(answerTuple.hasHadHeartAttack)
        assertThat(cursor.string("hasHadStroke")).isEqualTo(answerTuple.hasHadStroke)
        assertThat(cursor.string("hasHadKidneyDisease")).isEqualTo(answerTuple.hasHadKidneyDisease)
        assertThat(cursor.string("hasDiabetes")).isEqualTo(answerTuple.hasDiabetes)
      }
    }

    val allUnknownAnswersUuid = UUID.fromString("b2e3b63f-9b09-4b8d-bfa7-af2ac5972786")
    val allYesAnswersUuid = UUID.fromString("c11af491-d125-45af-9b3c-aa73510960c3")
    val allNoAnswersUuid = UUID.fromString("05384716-65d7-4261-8bcf-f2faecf62bb2")
    val mixedAnswersUuid = UUID.fromString("f0a50cf4-cf78-450a-979a-00df585fd642")

    val (allUnknownAnswersBeforeMigration, allUnknownAnswersAfterMigration) = AnswerTuple(
        medicalHistoryUuid = allUnknownAnswersUuid,
        diagnosedWithHypertension = "UNKNOWN",
        isOnTreatmentForHypertension = "UNKNOWN",
        hasHadHeartAttack = "UNKNOWN",
        hasHadStroke = "UNKNOWN",
        hasHadKidneyDisease = "UNKNOWN",
        hasDiabetes = "UNKNOWN"
    ) to AnswerTuple(
        medicalHistoryUuid = allUnknownAnswersUuid,
        diagnosedWithHypertension = "unknown",
        isOnTreatmentForHypertension = "unknown",
        hasHadHeartAttack = "unknown",
        hasHadStroke = "unknown",
        hasHadKidneyDisease = "unknown",
        hasDiabetes = "unknown"
    )

    val (allYesAnswersBeforeMigration, allYesAnswersAfterMigration) = AnswerTuple(
        medicalHistoryUuid = allYesAnswersUuid,
        diagnosedWithHypertension = "YES",
        isOnTreatmentForHypertension = "YES",
        hasHadHeartAttack = "YES",
        hasHadStroke = "YES",
        hasHadKidneyDisease = "YES",
        hasDiabetes = "YES"
    ) to AnswerTuple(
        medicalHistoryUuid = allYesAnswersUuid,
        diagnosedWithHypertension = "yes",
        isOnTreatmentForHypertension = "yes",
        hasHadHeartAttack = "yes",
        hasHadStroke = "yes",
        hasHadKidneyDisease = "yes",
        hasDiabetes = "yes"
    )

    val (allNoAnswersBeforeMigration, allNoAnswersAfterMigration) = AnswerTuple(
        medicalHistoryUuid = allNoAnswersUuid,
        diagnosedWithHypertension = "NO",
        isOnTreatmentForHypertension = "NO",
        hasHadHeartAttack = "NO",
        hasHadStroke = "NO",
        hasHadKidneyDisease = "NO",
        hasDiabetes = "NO"
    ) to AnswerTuple(
        medicalHistoryUuid = allNoAnswersUuid,
        diagnosedWithHypertension = "no",
        isOnTreatmentForHypertension = "no",
        hasHadHeartAttack = "no",
        hasHadStroke = "no",
        hasHadKidneyDisease = "no",
        hasDiabetes = "no"
    )

    val (mixedAnswersBeforeMigration, mixedAnswersAfterMigration) = AnswerTuple(
        medicalHistoryUuid = mixedAnswersUuid,
        diagnosedWithHypertension = "YES",
        isOnTreatmentForHypertension = "NO",
        hasHadHeartAttack = "UNKNOWN",
        hasHadStroke = "UNKNOWN",
        hasHadKidneyDisease = "NO",
        hasDiabetes = "YES"
    ) to AnswerTuple(
        medicalHistoryUuid = mixedAnswersUuid,
        diagnosedWithHypertension = "yes",
        isOnTreatmentForHypertension = "no",
        hasHadHeartAttack = "unknown",
        hasHadStroke = "unknown",
        hasHadKidneyDisease = "no",
        hasDiabetes = "yes"
    )

    val db_v39 = helper.createDatabase(39)
    listOf(
        allUnknownAnswersBeforeMigration,
        allYesAnswersBeforeMigration,
        allNoAnswersBeforeMigration,
        mixedAnswersBeforeMigration
    ).forEach { saveMedicalHistory(db = db_v39, answerTuple = it) }

    val db_v40 = helper.migrateTo(40)
    listOf(
        allUnknownAnswersAfterMigration,
        allYesAnswersAfterMigration,
        allNoAnswersAfterMigration,
        mixedAnswersAfterMigration
    ).forEach { verifyMedicalHistory(db = db_v40, answerTuple = it) }
  }

  @Test
  fun migrate_gender_from_40_to_41() {
    fun savePatient(db: SupportSQLiteDatabase, patientUuid: UUID, gender: String) {
      val addressUuid = UUID.randomUUID()

      db.execSQL("""
        INSERT INTO "PatientAddress" VALUES(
          '$addressUuid',
          'colony or village',
          'district',
          'state',
          'country',
          '2018-09-25T11:20:42.008Z',
          '2018-09-25T11:20:42.008Z',
          NULL
        )
      """)

      db.execSQL("""
        INSERT INTO "Patient" (
            "uuid", "addressUuid", "fullName", "searchableName",
            "gender", "dateOfBirth", "age_value", "age_updatedAt",
            "age_computedDateOfBirth", "status", "createdAt",
            "updatedAt", "deletedAt", "recordedAt", "syncStatus"
        ) VALUES (
            '$patientUuid', '$addressUuid', 'full-name', 'searchable-name',
            '$gender', 'date-of-birth', 'age-value', 'age-updated-at',
            'age-computed-date-of-birth', 'status', 'created-at',
            'updated-at', 'deleted-at', 'recorded-at', 'sync-status'
        )
      """)
    }

    fun verifyPatient(db: SupportSQLiteDatabase, patientUuid: UUID, expectedGenderString: String) {
      db.query("""
        SELECT * FROM "Patient" WHERE "uuid" = '$patientUuid'
      """).use { cursor ->
        cursor.moveToNext()

        assertThat(cursor.string("gender"))
            .isEqualTo(expectedGenderString)
      }
    }

    val dbV40 = helper.createDatabase(40)
    val maleUuid = UUID.fromString("b2e3b63f-9b09-4b8d-bfa7-af2ac5972786")
    val femaleUuid = UUID.fromString("c11af491-d125-45af-9b3c-aa73510960c3")
    val transgenderUuid = UUID.fromString("05384716-65d7-4261-8bcf-f2faecf62bb2")

    savePatient(dbV40, maleUuid, "MALE")
    savePatient(dbV40, femaleUuid, "FEMALE")
    savePatient(dbV40, transgenderUuid, "TRANSGENDER")

    val dbV41 = helper.migrateTo(41)
    verifyPatient(dbV41, maleUuid, "male")
    verifyPatient(dbV41, femaleUuid, "female")
    verifyPatient(dbV41, transgenderUuid, "transgender")
  }

  @Test
  fun migrate_patient_phone_number_type_from_41_to_42() {
    fun savePatientPhoneNumber(db: SupportSQLiteDatabase, phoneNumberUuid: UUID, phoneNumberType: String) {
      val addressUuid = UUID.randomUUID()
      val patientUuid = UUID.randomUUID()

      db.execSQL("""
        INSERT INTO "PatientAddress" VALUES(
          '$addressUuid',
          'colony-or-village',
          'district',
          'state',
          'country',
          '2018-09-25T11:20:42.008Z',
          '2018-09-25T11:20:42.008Z',
          NULL
        )
      """)

      db.execSQL("""
        INSERT INTO "Patient" (
            "uuid", "addressUuid", "fullName", "searchableName",
            "gender", "dateOfBirth", "age_value", "age_updatedAt",
            "age_computedDateOfBirth", "status", "createdAt",
            "updatedAt", "deletedAt", "recordedAt", "syncStatus"
        ) VALUES (
            '$patientUuid', '$addressUuid', 'full-name', 'searchable-name',
            'gender', 'date-of-birth', 'age-value', 'age-updated-at',
            'age-computed-date-of-birth', 'status', 'created-at',
            'updated-at', 'deleted-at', 'recorded-at', 'sync-status'
        )
      """)

      db.execSQL("""
        INSERT INTO "PatientPhoneNumber" VALUES(
          '$phoneNumberUuid', '$patientUuid', 'number', '$phoneNumberType',
          'true', '2018-09-25T11:20:42.008Z', '2018-09-25T11:20:42.008Z', NULL
        )
      """)
    }

    fun verifyPatientPhoneNumber(db: SupportSQLiteDatabase, phoneNumberUuid: UUID, expectedPhoneNumberType: String) {
      db.query("""
        SELECT * FROM "PatientPhoneNumber" WHERE "uuid" = '$phoneNumberUuid'
      """).use { cursor ->
        cursor.moveToNext()

        assertThat(cursor.string("phoneType")).isEqualTo(expectedPhoneNumberType)
      }
    }

    val mobileUuid = UUID.fromString("b2e3b63f-9b09-4b8d-bfa7-af2ac5972786")
    val landlineUuid = UUID.fromString("c11af491-d125-45af-9b3c-aa73510960c3")

    val dbV41 = helper.createDatabase(41)
    savePatientPhoneNumber(dbV41, mobileUuid, "MOBILE")
    savePatientPhoneNumber(dbV41, landlineUuid, "LANDLINE")

    val dbV42 = helper.migrateTo(42)
    verifyPatientPhoneNumber(dbV42, mobileUuid, "mobile")
    verifyPatientPhoneNumber(dbV42, landlineUuid, "landline")
  }

  @Test
  fun drop_communication_table_from_42_to_43() {
    val dbV42 = helper.createDatabase(42)
    dbV42.assertTableExists("Communication")

    val dbV43 = helper.migrateTo(43)
    dbV43.assertTableDoesNotExist("Communication")
  }

  @Test
  fun migrating_ongoing_login_entry_to_44_should_add_the_new_columns() {
    val db_v43 = helper.createDatabase(43)
    val tableName = "OngoingLoginEntry"

    db_v43.assertColumns(
        tableName = tableName,
        expectedColumns = setOf("uuid", "phoneNumber", "pin")
    )

    val db_v44 = helper.migrateTo(44)

    db_v44.assertColumns(
        tableName = tableName,
        expectedColumns = setOf(
            "uuid", "phoneNumber", "pin",
            "fullName", "pinDigest", "registrationFacilityUuid",
            "status", "createdAt", "updatedAt"
        )
    )
  }

  @Test
  fun migrating_existing_ongoing_login_entry_to_44_should_migrate_exiting_data_from_not_logged_in_user() {
    fun createFacility(
        db: SupportSQLiteDatabase,
        facilityUuid: UUID
    ) {
      require(db.version == 43) { "Required DB version: 43; Found: ${db.version}" }
      db.execSQL("""
      INSERT INTO "Facility"
      VALUES (
        '$facilityUuid',
        'Facility',
        'Facility type',
        'Street address',
        'Village or colony',
        'District',
        'State',
        'Country',
        'Pin code',
        NULL,
        NULL,
        '2018-09-25T11:20:42.008Z',
        '2018-09-25T11:20:42.008Z',
        'PENDING',
        NULL,
        NULL,
        NULL
      )
    """
      )
    }

    fun createUser(
        db: SupportSQLiteDatabase,
        userUuid: String,
        facilityUuid: UUID,
        name: String,
        phoneNumber: String,
        pinDigest: String,
        status: String,
        createdAt: String,
        updatedAt: String,
        loggedInStatus: User.LoggedInStatus
    ) {
      require(db.version == 43) { "Required DB version: 43; Found: ${db.version}" }
      db.execSQL("""
        INSERT INTO "LoggedInUser"
        VALUES (
          '$userUuid',
          '$name',
          '$phoneNumber',
          '$pinDigest',
          '$status',
          '$createdAt',
          '$updatedAt',
          '$loggedInStatus'
        )
      """)

      db.execSQL("""
        INSERT INTO "LoggedInUserFacilityMapping"
        VALUES (
          '$userUuid',
          '$facilityUuid',
          1
        )
      """)
    }

    fun createLoginEntry(
        db: SupportSQLiteDatabase,
        userUuid: String,
        pin: String
    ) {
      require(db.version == 43) { "Required DB version: 43; Found: ${db.version}" }
      db.execSQL("""
        INSERT INTO "OngoingLoginEntry"
        VALUES (
          '$userUuid',
          '',
          '$pin'
        )
      """)
    }

    fun verifyLoginEntryPresent(
        db: SupportSQLiteDatabase,
        userUuid: String,
        facilityUuid: UUID,
        name: String,
        phoneNumber: String,
        pin: String,
        pinDigest: String,
        status: String,
        createdAt: String,
        updatedAt: String
    ) {
      require(db.version == 44) { "Required DB version: 44; Found: ${db.version}" }
      db.query("""
        SELECT * FROM "OngoingLoginEntry"
        WHERE "uuid" = '$userUuid'
      """
      ).use { cursor ->
        assertThat(cursor.count).isEqualTo(1)
        cursor.moveToFirst()

        assertThat(cursor.string("fullName")).isEqualTo(name)
        assertThat(cursor.string("phoneNumber")).isEqualTo(phoneNumber)
        assertThat(cursor.string("pin")).isEqualTo(pin)
        assertThat(cursor.string("pinDigest")).isEqualTo(pinDigest)
        assertThat(cursor.string("registrationFacilityUuid")).isEqualTo(facilityUuid.toString())
        assertThat(cursor.string("status")).isEqualTo(status)
        assertThat(cursor.string("createdAt")).isEqualTo(createdAt)
        assertThat(cursor.string("updatedAt")).isEqualTo(updatedAt)
      }
    }

    fun verifyLoginEntryAbsent(db: SupportSQLiteDatabase, userUuid: String) {
      require(db.version == 44) { "Required DB version: 44; Found: ${db.version}" }
      db.query("""
        SELECT * FROM "OngoingLoginEntry"
        WHERE "uuid" = '$userUuid'
      """
      ).use { cursor ->
        assertThat(cursor.count).isEqualTo(0)
      }
    }

    fun verifyUserPresent(db: SupportSQLiteDatabase, userUuid: String) {
      require(db.version == 44) { "Required DB version: 44; Found: ${db.version}" }
      db.query("""
        SELECT * FROM "LoggedInUser"
        WHERE "uuid" = '$userUuid'
      """
      ).use { cursor ->
        assertThat(cursor.count).isEqualTo(1)
      }
    }

    fun verifyUserAbsent(db: SupportSQLiteDatabase, userUuid: String) {
      require(db.version == 44) { "Required DB version: 44; Found: ${db.version}" }
      db.query("""
        SELECT * FROM "LoggedInUser"
        WHERE "uuid" = '$userUuid'
      """
      ).use { cursor ->
        assertThat(cursor.count).isEqualTo(0)
      }
    }

    data class TestCase(
        val uuid: UUID,
        val name: String,
        val phoneNumber: String,
        val pinDigest: String,
        val status: String,
        val createdAt: String,
        val updatedAt: String,
        val loggedInStatus: User.LoggedInStatus,
        val pin: String,
        val createUserBeforeMigration: Boolean = true,
        val createLoginEntryBeforeMigration: Boolean = true,
        val shouldUserBePresentAfterMigration: Boolean,
        val shouldLoginEntryBePresentAfterMigration: Boolean
    )

    val notLoggedInUserTestCase = TestCase(
        uuid = UUID.fromString("b2e149db-6184-4ad1-9bd9-333a3bfd2061"),
        name = "Not Logged In User",
        phoneNumber = "phone 1",
        pinDigest = "pin digest 1",
        status = "allowed",
        createdAt = "created 1",
        updatedAt = "updated 1",
        loggedInStatus = User.LoggedInStatus.NOT_LOGGED_IN,
        pin = "pin 1",
        shouldUserBePresentAfterMigration = false,
        shouldLoginEntryBePresentAfterMigration = false
    )
    val otpRequestedUserTestCase = TestCase(
        uuid = UUID.fromString("af518d41-2a3b-48df-8c89-dda9b3c721b5"),
        name = "OTP Requested User",
        phoneNumber = "phone 2",
        pinDigest = "pin digest 2",
        status = "allowed",
        createdAt = "created 2",
        updatedAt = "updated 2",
        loggedInStatus = User.LoggedInStatus.OTP_REQUESTED,
        pin = "pin 2",
        shouldUserBePresentAfterMigration = true,
        shouldLoginEntryBePresentAfterMigration = true
    )
    val loggedInUserTestCase = TestCase(
        uuid = UUID.fromString("6da6169b-c235-41d3-b252-70343aae7df9"),
        name = "Logged In User",
        phoneNumber = "phone 3",
        pinDigest = "pin digest 3",
        status = "allowed",
        createdAt = "created 3",
        updatedAt = "updated 3",
        loggedInStatus = User.LoggedInStatus.LOGGED_IN,
        pin = "pin 3",
        shouldUserBePresentAfterMigration = true,
        shouldLoginEntryBePresentAfterMigration = false
    )
    val resettingPinUserTestCase = TestCase(
        uuid = UUID.fromString("462ea175-0f42-4662-a4aa-b68bb92482b1"),
        name = "Resetting PIN User",
        phoneNumber = "phone 4",
        pinDigest = "pin digest 4",
        status = "allowed",
        createdAt = "created 4",
        updatedAt = "updated 4",
        loggedInStatus = User.LoggedInStatus.RESETTING_PIN,
        pin = "pin 4",
        shouldUserBePresentAfterMigration = true,
        shouldLoginEntryBePresentAfterMigration = false
    )
    val resetPinRequestedUserTestCase = TestCase(
        uuid = UUID.fromString("0a01ea29-7860-4d75-b595-7a1460f7082c"),
        name = "Reset PIN Requested User",
        phoneNumber = "phone 5",
        pinDigest = "pin digest 5",
        status = "allowed",
        createdAt = "created 5",
        updatedAt = "updated 5",
        loggedInStatus = User.LoggedInStatus.RESET_PIN_REQUESTED,
        pin = "pin 5",
        shouldUserBePresentAfterMigration = true,
        shouldLoginEntryBePresentAfterMigration = false
    )
    val unauthorizedTestCase = TestCase(
        uuid = UUID.fromString("534b6bb1-1b12-4e68-95dd-faa68a1cc3b0"),
        name = "Unauthorized User",
        phoneNumber = "phone 6",
        pinDigest = "pin digest 6",
        status = "allowed",
        createdAt = "created 6",
        updatedAt = "updated 6",
        loggedInStatus = User.LoggedInStatus.UNAUTHORIZED,
        pin = "pin 6",
        shouldUserBePresentAfterMigration = true,
        shouldLoginEntryBePresentAfterMigration = false
    )
    val userNotPresentTestCase = TestCase(
        uuid = UUID.fromString("fee6e4a8-fdea-4f4f-8e12-95c66978e7bf"),
        name = "Not Present User",
        phoneNumber = "phone 7",
        pinDigest = "pin digest 7",
        status = "allowed",
        createdAt = "created 7",
        updatedAt = "updated 7",
        loggedInStatus = User.LoggedInStatus.NOT_LOGGED_IN,
        pin = "pin 7",
        createUserBeforeMigration = false,
        shouldUserBePresentAfterMigration = false,
        shouldLoginEntryBePresentAfterMigration = false
    )
    val newlyRegisteredUserTestCase = TestCase(
        uuid = UUID.fromString("238d4b62-b043-4e55-a2f9-8385dc745bdd"),
        name = "Newly Registered User",
        phoneNumber = "phone 8",
        pinDigest = "pin digest 8",
        status = "requested",
        createdAt = "created 8",
        updatedAt = "updated 8",
        loggedInStatus = User.LoggedInStatus.NOT_LOGGED_IN,
        pin = "pin 8",
        createLoginEntryBeforeMigration = false,
        shouldUserBePresentAfterMigration = false,
        shouldLoginEntryBePresentAfterMigration = false
    )

    val testCases = listOf(
        notLoggedInUserTestCase,
        otpRequestedUserTestCase,
        loggedInUserTestCase,
        resettingPinUserTestCase,
        resetPinRequestedUserTestCase,
        unauthorizedTestCase,
        userNotPresentTestCase,
        newlyRegisteredUserTestCase
    )

    testCases.forEach { testCase ->
      // given
      val db_v43 = helper.createDatabase(43)
      val facilityUuid = UUID.fromString("77cf0466-6446-473a-830d-ca49bb1607f4")
      createFacility(db = db_v43, facilityUuid = facilityUuid)

      if (testCase.createUserBeforeMigration) {
        createUser(
            db = db_v43,
            userUuid = testCase.uuid.toString(),
            facilityUuid = facilityUuid,
            name = testCase.name,
            phoneNumber = testCase.phoneNumber,
            pinDigest = testCase.pinDigest,
            status = testCase.status,
            createdAt = testCase.createdAt,
            updatedAt = testCase.updatedAt,
            loggedInStatus = testCase.loggedInStatus
        )
      }

      if (testCase.createLoginEntryBeforeMigration) {
        createLoginEntry(
            db = db_v43,
            userUuid = testCase.uuid.toString(),
            pin = testCase.pin
        )
      }

      // when
      val db_v44 = helper.migrateTo(44)

      // then
      if (testCase.shouldUserBePresentAfterMigration) {
        verifyUserPresent(db = db_v44, userUuid = testCase.uuid.toString())
      } else {
        verifyUserAbsent(db = db_v44, userUuid = testCase.uuid.toString())
      }

      if (testCase.shouldLoginEntryBePresentAfterMigration) {
        verifyLoginEntryPresent(
            db = db_v44,
            userUuid = testCase.uuid.toString(),
            facilityUuid = facilityUuid,
            name = testCase.name,
            phoneNumber = testCase.phoneNumber,
            pin = testCase.pin,
            pinDigest = testCase.pinDigest,
            status = testCase.status,
            createdAt = testCase.createdAt,
            updatedAt = testCase.updatedAt
        )
      } else {
        verifyLoginEntryAbsent(db = db_v44, userUuid = testCase.uuid.toString())
      }
    }
  }

  @Test
  fun migrating_to_45_should_remove_the_columns_from_the_Patient_table() {
    // given
    val db_v44 = helper.createDatabase(44)
    val tableName = "Patient"
    db_v44.assertColumns(
        tableName = tableName,
        expectedColumns = setOf(
            "uuid", "addressUuid", "fullName", "searchableName", "gender",
            "dateOfBirth", "age_value", "age_updatedAt", "age_computedDateOfBirth",
            "status", "createdAt", "updatedAt", "deletedAt", "recordedAt", "syncStatus"
        )
    )

    // when
    val db_v45 = helper.migrateTo(45)

    // then
    db_v45.assertColumns(
        tableName = tableName,
        expectedColumns = setOf(
            "uuid", "addressUuid", "fullName", "gender",
            "dateOfBirth", "age_value", "age_updatedAt",
            "status", "createdAt", "updatedAt", "deletedAt", "recordedAt", "syncStatus"
        )
    )
  }

  @Test
  fun migrating_to_45_should_not_remove_any_of_the_existing_data_from_the_patient_table() {
    // given
    val db_v44 = helper.createDatabase(44)

    val instant = Instant.parse("2018-01-01T00:00:00.000Z")

    val patientAddressUuid = "05f9c798-1701-4379-b14a-b1b18d937a33"
    db_v44.insert("PatientAddress", mapOf(
        "uuid" to patientAddressUuid,
        "colonyOrVillage" to "Colony",
        "district" to "District",
        "state" to "State",
        "country" to "Country",
        "createdAt" to instant.toString(),
        "updatedAt" to instant.toString(),
        "deletedAt" to null
    ))

    val patientUuid = "0b18af95-e73f-43ea-9838-34abe9d7858e"
    val name = "Anish Acharya"
    val searchableName = "anishacharya"
    val gender = "male"
    val dateOfBirth = "1990-01-01"
    val ageValue = 40
    val ageUpdatedAt = instant.toString()
    val ageComputedDateofBirth = "1990-01-02"
    val status = "active"
    val patientCreatedAt = instant.plusSeconds(1).toString()
    val patientUpdatedAt = instant.plusSeconds(2).toString()
    val patientRecordedAt = instant.minusSeconds(1).toString()
    val syncStatus = "PENDING"

    db_v44.insert(
        "Patient",
        mapOf(
            "uuid" to patientUuid,
            "addressUuid" to patientAddressUuid,
            "fullName" to name,
            "searchableName" to searchableName,
            "gender" to gender,
            "dateOfBirth" to dateOfBirth,
            "age_value" to ageValue,
            "age_updatedAt" to ageUpdatedAt,
            "age_computedDateOfBirth" to ageComputedDateofBirth,
            "status" to status,
            "createdAt" to patientCreatedAt,
            "updatedAt" to patientUpdatedAt,
            "recordedAt" to patientRecordedAt,
            "syncStatus" to syncStatus
        )
    )

    val patientPhoneNumberUuid = "d039c3cd-7c8c-49e5-8f58-edc482728a46"
    db_v44.insert(
        "PatientPhoneNumber",
        mapOf(
            "uuid" to patientPhoneNumberUuid,
            "patientUuid" to patientUuid,
            "number" to "123456",
            "phoneType" to "mobile",
            "active" to true,
            "createdAt" to instant.toString(),
            "updatedAt" to instant.toString(),
            "deletedAt" to null
        )
    )

    val businessIdUuid = "07e6bb07-46ad-46c6-bb46-ec939ba96ccd"
    db_v44.insert(
        "BusinessId",
        mapOf(
            "uuid" to businessIdUuid,
            "patientUuid" to patientUuid,
            "identifier" to "fdc0706b-3cbf-4644-9cf3-c5b794514dd6",
            "identifierType" to "simple_bp_passport",
            "metaVersion" to "org.simple.bppassport.meta.v1",
            "meta" to "",
            "createdAt" to instant.toString(),
            "updatedAt" to instant.toString(),
            "deletedAt" to null
        )
    )

    // when
    val db_v45 = helper.migrateTo(45)

    // then
    val expectedPatientTableAfterMigration = mapOf(
        "uuid" to patientUuid,
        "addressUuid" to patientAddressUuid,
        "fullName" to name,
        "gender" to gender,
        "dateOfBirth" to dateOfBirth,
        "age_value" to ageValue,
        "age_updatedAt" to ageUpdatedAt,
        "status" to status,
        "createdAt" to patientCreatedAt,
        "updatedAt" to patientUpdatedAt,
        "deletedAt" to null,
        "recordedAt" to patientRecordedAt,
        "syncStatus" to syncStatus
    )
    db_v45
        .query("SELECT * FROM Patient")
        .use { cursor ->
          with(cursor) {
            assertThat(count).isEqualTo(1)
            moveToFirst()
            assertValues(expectedPatientTableAfterMigration)
          }
        }

    db_v45
        .query("""SELECT "uuid" FROM "PatientPhoneNumber" WHERE uuid == '$patientPhoneNumberUuid'""")
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(1)
        }

    db_v45
        .query("""SELECT "uuid" FROM "BusinessId" WHERE uuid == '$businessIdUuid'""")
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(1)
        }
  }

  @Test
  fun migrating_to_45_should_maintain_the_foreign_key_references_to_the_patient_table() {
    // given
    val db_v44 = helper.createDatabase(44)

    val instant = Instant.parse("2018-01-01T00:00:00.000Z")

    val patientAddressUuid = "05f9c798-1701-4379-b14a-b1b18d937a33"
    val patientUuid = "0b18af95-e73f-43ea-9838-34abe9d7858e"
    val patientPhoneNumberUuid = "d039c3cd-7c8c-49e5-8f58-edc482728a46"
    val businessIdUuid = "07e6bb07-46ad-46c6-bb46-ec939ba96ccd"

    db_v44.insert("PatientAddress", mapOf(
        "uuid" to patientAddressUuid,
        "colonyOrVillage" to "Colony",
        "district" to "District",
        "state" to "State",
        "country" to "Country",
        "createdAt" to instant.toString(),
        "updatedAt" to instant.toString(),
        "deletedAt" to null
    ))

    db_v44.insert(
        "Patient",
        mapOf(
            "uuid" to patientUuid,
            "addressUuid" to patientAddressUuid,
            "fullName" to "Anish Acharya",
            "searchableName" to "anishacharya",
            "gender" to "male",
            "dateOfBirth" to "1990-01-01",
            "age_value" to 40,
            "age_updatedAt" to instant.toString(),
            "age_computedDateOfBirth" to "1990-01-02",
            "status" to "active",
            "createdAt" to instant.plusSeconds(1).toString(),
            "updatedAt" to instant.plusSeconds(2).toString(),
            "recordedAt" to instant.minusSeconds(1).toString(),
            "syncStatus" to "PENDING"
        )
    )

    db_v44.insert(
        "PatientPhoneNumber",
        mapOf(
            "uuid" to patientPhoneNumberUuid,
            "patientUuid" to patientUuid,
            "number" to "123456",
            "phoneType" to "mobile",
            "active" to true,
            "createdAt" to instant.toString(),
            "updatedAt" to instant.toString(),
            "deletedAt" to null
        )
    )

    db_v44.insert(
        "BusinessId",
        mapOf(
            "uuid" to businessIdUuid,
            "patientUuid" to patientUuid,
            "identifier" to "fdc0706b-3cbf-4644-9cf3-c5b794514dd6",
            "identifierType" to "simple_bp_passport",
            "metaVersion" to "org.simple.bppassport.meta.v1",
            "meta" to "",
            "createdAt" to instant.toString(),
            "updatedAt" to instant.toString(),
            "deletedAt" to null
        )
    )
    val db_v45 = helper.migrateTo(45)

    // when
    db_v45.execSQL("""DELETE FROM "PatientAddress" WHERE "uuid" = '$patientAddressUuid'""")

    // then
    db_v45
        .query("""SELECT "uuid" FROM "Patient" WHERE "uuid" == '$patientUuid'""")
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(0)
        }

    db_v45
        .query("""SELECT "uuid" FROM "PatientPhoneNumber" WHERE "uuid" == '$patientPhoneNumberUuid'""")
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(0)
        }

    db_v45
        .query("""SELECT "uuid" FROM "BusinessId" WHERE "uuid" == '$businessIdUuid'""")
        .use { cursor ->
          assertThat(cursor.count).isEqualTo(0)
        }
  }
}

private fun Cursor.string(column: String): String? = getString(getColumnIndex(column))
private fun Cursor.boolean(column: String): Boolean? = getInt(getColumnIndex(column)) == 1
private fun Cursor.integer(columnName: String): Int? = getInt(getColumnIndex(columnName))
private fun Cursor.long(columnName: String): Long = getLong(getColumnIndex(columnName))
private fun Cursor.double(columnName: String): Double = getDouble(getColumnIndex(columnName))
private fun Cursor.float(columnName: String): Float = getFloat(getColumnIndex(columnName))

private fun SupportSQLiteDatabase.assertColumnCount(tableName: String, expectedCount: Int) {
  this.query("""
      SELECT * FROM "$tableName"
    """).use {
    assertWithMessage("With table [$tableName]").that(it.columnCount).isEqualTo(expectedCount)
  }
}

private fun SupportSQLiteDatabase.assertTableDoesNotExist(tableName: String) {
  query("""
    SELECT DISTINCT "tbl_name" FROM "sqlite_master" WHERE "tbl_name"='$tableName'
    """).use {
    assertWithMessage("Expected that [$tableName] does not exist, but found it exists").that(it.count).isEqualTo(0)
  }
}

private fun SupportSQLiteDatabase.assertTableExists(tableName: String) {
  query("""
    SELECT DISTINCT "tbl_name" FROM "sqlite_master" WHERE "tbl_name"='$tableName'
    """).use {
    assertWithMessage("Expected that [$tableName] exists, but found it does not exist").that(it.count).isEqualTo(1)
  }
}

private fun SupportSQLiteDatabase.assertColumns(tableName: String, expectedColumns: Set<String>) {
  query("""
    SELECT * FROM "$tableName" LIMIT 0
  """).use { cursor ->
    val columnsPresentInDatabase = cursor.columnNames.toSet()
    assertThat(columnsPresentInDatabase).isEqualTo(expectedColumns)
  }
}

private fun SupportSQLiteDatabase.insert(tableName: String, valuesMap: Map<String, Any?>) {
  val contentValues = valuesMap
      .entries
      .fold(ContentValues()) { values, (key, value) ->
        when (value) {
          null -> values.putNull(key)
          is Int -> values.put(key, value)
          is Long -> values.put(key, value)
          is Float -> values.put(key, value)
          is Double -> values.put(key, value)
          is Boolean -> values.put(key, value)
          is String -> values.put(key, value)
          else -> throw IllegalArgumentException("Unknown type (${value.javaClass.name}) for key: $key")
        }

        values
      }

  insert(tableName, SQLiteDatabase.CONFLICT_ABORT, contentValues)
}

private fun Cursor.assertValues(valuesMap: Map<String, Any?>) {
  assertThat(columnNames.toSet()).containsExactlyElementsIn(valuesMap.keys)
  valuesMap
      .forEach { (key, value) ->
        val withMessage = assertWithMessage("For column [$key]: ")
        when (value) {
          null -> withMessage.that(isNull(getColumnIndex(key))).isTrue()
          is Int -> withMessage.that(integer(key)).isEqualTo(value)
          is Long -> withMessage.that(long(key)).isEqualTo(value)
          is Float -> withMessage.that(float(key)).isEqualTo(value)
          is Double -> withMessage.that(double(key)).isEqualTo(value)
          is Boolean -> withMessage.that(boolean(key)).isEqualTo(value)
          is String -> withMessage.that(string(key)).isEqualTo(value)
          else -> throw IllegalArgumentException("Unknown type (${value.javaClass.name}) for key: $key")
        }
      }
}
