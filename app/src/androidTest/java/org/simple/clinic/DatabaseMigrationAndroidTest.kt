package org.simple.clinic

import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.runner.AndroidJUnit4
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.simple.clinic.storage.inTransaction
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@Suppress("LocalVariableName")
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationAndroidTest {

  @get:Rule
  val helper = MigrationTestHelperWithForeignConstraints()

  @get:Rule
  val expectedException = ExpectedException.none()

  @Inject
  lateinit var migrations: ArrayList<Migration>

  @Inject
  @field:Named("last_facility_pull_token")
  lateinit var lastFacilityPullToken: Preference<Optional<String>>

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    helper.migrations = migrations
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
}

private fun Cursor.string(column: String): String? = getString(getColumnIndex(column))
private fun Cursor.boolean(column: String): Boolean? = getInt(getColumnIndex(column)) == 1
private fun Cursor.integer(columnName: String): Int? = getInt(getColumnIndex(columnName))
private fun Cursor.double(columnName: String): Double = getDouble(getColumnIndex(columnName))

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
