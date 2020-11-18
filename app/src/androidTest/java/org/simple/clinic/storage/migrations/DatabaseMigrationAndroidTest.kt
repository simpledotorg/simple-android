package org.simple.clinic.storage.migrations

import android.database.sqlite.SQLiteConstraintException
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.f2prateek.rx.preferences2.Preference
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.RuleChain
import org.simple.clinic.MigrationTestHelperWithForeignKeyConstraints
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.assertColumnCount
import org.simple.clinic.assertColumns
import org.simple.clinic.assertTableDoesNotExist
import org.simple.clinic.assertTableExists
import org.simple.clinic.assertValues
import org.simple.clinic.boolean
import org.simple.clinic.double
import org.simple.clinic.insert
import org.simple.clinic.integer
import org.simple.clinic.storage.inTransaction
import org.simple.clinic.string
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.Rules
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.generateEncounterUuid
import org.simple.clinic.util.toLocalDateAtZone
import org.simple.clinic.uuid
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@Suppress("LocalVariableName")
class DatabaseMigrationAndroidTest {

  private val helper = MigrationTestHelperWithForeignKeyConstraints()

  private val expectedException: ExpectedException = ExpectedException.none()

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(expectedException)
      .around(helper)

  @Inject
  lateinit var migrations: List<@JvmSuppressWildcards Migration>

  @Inject
  @Named("last_facility_pull_token")
  lateinit var lastFacilityPullToken: Preference<Optional<String>>

  @Inject
  @Named("last_patient_pull_token")
  lateinit var lastPatientPullToken: Preference<Optional<String>>

  @Inject
  lateinit var clock: TestUtcClock

  @Inject
  lateinit var testData: TestData

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    clock.setDate(LocalDate.parse("2000-01-01"))

    helper.migrations = migrations

    lastFacilityPullToken.set(None())
    lastPatientPullToken.set(None())
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
    /*
     * Originally, this test used the `libspellfix` sqlite extension to create a virtual table for testing
     * the sqlite fuzzy search feature. Since then, we have removed the module, but this migration
     * test was failing because the extension was no longer present.
     *
     * Since there are no active users in production (at the time of this change) who are on an older
     * database version, we can no-op this migration and not test it because maintaining it is not
     * worth the effort.
     **/
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

    assertThat(lastFacilityPullToken.get()).isEqualTo(None<String>())
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
    assertThat(lastPatientPullToken.get()).isEqualTo(None<String>())
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
        loggedInStatus: String
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
        val loggedInStatus: String,
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
        loggedInStatus = "NOT_LOGGED_IN",
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
        loggedInStatus = "OTP_REQUESTED",
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
        loggedInStatus = "LOGGED_IN",
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
        loggedInStatus = "RESETTING_PIN",
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
        loggedInStatus = "RESET_PIN_REQUESTED",
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
        loggedInStatus = "UNAUTHORIZED",
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
        loggedInStatus = "NOT_LOGGED_IN",
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
        loggedInStatus = "NOT_LOGGED_IN",
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
  fun migration_to_46_should_create_table_HomescreenIllustration() {
    val db_v45 = helper.createDatabase(45)
    db_v45.assertTableDoesNotExist("HomescreenIllustration")
    val db_v46 = helper.migrateTo(46)
    db_v46.assertTableExists("HomescreenIllustration")
    db_v46.assertColumns(
        "HomescreenIllustration",
        setOf("eventId", "illustrationUrl", "from_day", "from_month", "to_day", "to_month")
    )
  }

  @Test
  fun verify_migration_to_47_drops_table_HomescreenIllustration() {
    val dbV46 = helper.createDatabase(46)
    dbV46.assertTableExists("HomescreenIllustration")

    val dbV47 = helper.migrateTo(47)
    dbV47.assertTableDoesNotExist("HomescreenIllustration")
  }

  @Test
  fun verify_migration_to_48_adds_granted_reminder_consent() {
    val instant = Instant.parse("2018-01-01T00:00:00.000Z")

    val patientAddressUuid = "05f9c798-1701-4379-b14a-b1b18d937a33"
    val patientUuid = "0b18af95-e73f-43ea-9838-34abe9d7858e"

    val dbV47 = helper.createDatabase(47)
    dbV47.assertColumns(
        "Patient",
        setOf("uuid", "addressUuid", "fullName", "gender", "dateOfBirth", "status", "createdAt", "updatedAt", "deletedAt", "recordedAt",
            "syncStatus", "age_value", "age_updatedAt")
    )

    dbV47.insert("PatientAddress", mapOf(
        "uuid" to patientAddressUuid,
        "colonyOrVillage" to "Colony",
        "district" to "District",
        "state" to "State",
        "country" to "Country",
        "createdAt" to instant.toString(),
        "updatedAt" to instant.toString(),
        "deletedAt" to null
    ))
    dbV47.insert(
        "Patient",
        mapOf(
            "uuid" to patientUuid,
            "addressUuid" to patientAddressUuid,
            "fullName" to "Anish Acharya",
            "gender" to "male",
            "dateOfBirth" to "1990-01-01",
            "age_value" to 40,
            "age_updatedAt" to instant.toString(),
            "status" to "active",
            "createdAt" to instant.plusSeconds(1).toString(),
            "updatedAt" to instant.plusSeconds(2).toString(),
            "recordedAt" to instant.minusSeconds(1).toString(),
            "syncStatus" to "PENDING"
        )
    )

    val dbV48 = helper.migrateTo(48)
    dbV48.assertColumns(
        "Patient",
        setOf("uuid", "addressUuid", "fullName", "gender", "dateOfBirth", "status", "createdAt", "updatedAt", "deletedAt", "recordedAt",
            "syncStatus", "age_value", "age_updatedAt", "reminderConsent")
    )
    dbV48.query("""SELECT * FROM "Patient"""").use {
      assertThat(it.count).isEqualTo(1)

      it.moveToNext()

      assertThat(it.string("reminderConsent")).isEqualTo("granted")
    }
  }

  @Test
  fun migration_from_48_to_49_should_create_encounter_table() {
    //given
    val encounterTable = "Encounter"
    val db_48 = helper.createDatabase(version = 48)
    db_48.assertTableDoesNotExist(encounterTable)

    //when
    val db_49 = helper.migrateTo(version = 49)

    //then
    db_49.assertTableExists(encounterTable)
    db_49.assertColumnCount(encounterTable, 6)
    db_49.assertColumns(encounterTable, setOf(
        "uuid",
        "patientUuid",
        "encounteredOn",
        "createdAt",
        "updatedAt",
        "deletedAt")
    )
  }

  @Test
  fun migration_from_49_to_50_create_only_one_encounter_for_per_day_but_add_encounterUuid_to_all_BPs() {
    //given
    val bloodPressureMeasurementTable = "BloodPressureMeasurement"
    val userUuid = UUID.fromString("8c1cfd1a-10bc-4aaf-a648-3d477527a444")
    val facilityUuid = UUID.fromString("0086ba86-314c-49d9-8822-2563e53bed92")
    val patientUuid = UUID.fromString("c4233170-feb2-3d34-8115-98fef5a68837")

    val earliestBpWhichIsDeleted = testData.bloodPressureMeasurement(
        uuid = UUID.fromString("3094fe79-41b8-4158-bb64-4043efd82475"),
        facilityUuid = facilityUuid,
        patientUuid = patientUuid,
        createdAt = Instant.parse("2018-07-04T01:59:59Z"),
        updatedAt = Instant.parse("2018-07-04T01:59:59Z"),
        recordedAt = Instant.parse("2018-07-03T00:00:00Z"),
        deletedAt = Instant.parse("2018-07-04T02:00:00Z"),
        userUuid = userUuid
    )
    val firstBp = testData.bloodPressureMeasurement(
        uuid = UUID.fromString("3eb96138-5270-419c-bcdf-86db95d0bc8b"),
        facilityUuid = facilityUuid,
        patientUuid = patientUuid,
        createdAt = Instant.parse("2018-07-04T02:00:00Z"),
        updatedAt = Instant.parse("2018-07-04T02:00:00Z"),
        recordedAt = Instant.parse("2018-07-03T00:00:00Z"),
        userUuid = userUuid
    )
    val secondBp = testData.bloodPressureMeasurement(
        uuid = UUID.fromString("a2455b0c-cf14-46c9-9f73-6e18972982b0"),
        facilityUuid = facilityUuid,
        patientUuid = patientUuid,
        createdAt = Instant.parse("2018-07-03T01:30:00Z"),
        updatedAt = Instant.parse("2018-07-03T01:30:00Z"),
        recordedAt = Instant.parse("2018-07-03T01:30:00Z"),
        userUuid = userUuid
    )
    val testRecords = listOf(earliestBpWhichIsDeleted, firstBp, secondBp)

    val db_49 = helper.createDatabase(version = 49)

    testRecords
        .map {
          mapOf(
              "uuid" to it.uuid,
              "systolic" to it.reading.systolic,
              "diastolic" to it.reading.diastolic,
              "syncStatus" to "DONE",
              "userUuid" to it.userUuid,
              "facilityUuid" to it.facilityUuid,
              "patientUuid" to it.patientUuid,
              "createdAt" to it.createdAt,
              "updatedAt" to it.updatedAt,
              "deletedAt" to it.deletedAt,
              "recordedAt" to it.recordedAt
          )
        }
        .forEach { db_49.insert(bloodPressureMeasurementTable, it) }

    //when
    val db_50 = helper.migrateTo(version = 50)

    //then
    val userClock = TestUserClock()
    val expectedEncounteredOn = firstBp.recordedAt.toLocalDateAtZone(userClock.zone)
    val expectedEncounterId = generateEncounterUuid(
        facilityUuid = facilityUuid,
        patientUuid = patientUuid,
        encounteredDate = expectedEncounteredOn
    )

    db_50.query("""SELECT * FROM '$bloodPressureMeasurementTable' """).use {
      assertThat(it.count).isEqualTo(3)
      assertThat(it.columnCount).isEqualTo(12)
      it.moveToFirst()
      assertThat(it.uuid("uuid")).isEqualTo(earliestBpWhichIsDeleted.uuid)
      assertThat(it.uuid("encounterUuid")).isEqualTo(expectedEncounterId)

      it.moveToNext()
      assertThat(it.uuid("uuid")).isEqualTo(firstBp.uuid)
      assertThat(it.uuid("encounterUuid")).isEqualTo(expectedEncounterId)

      it.moveToNext()
      assertThat(it.uuid("uuid")).isEqualTo(secondBp.uuid)
      assertThat(it.uuid("encounterUuid")).isEqualTo(expectedEncounterId)
    }

    val expectedEncounterValues = mapOf(
        "uuid" to expectedEncounterId,
        "patientUuid" to patientUuid,
        "encounteredOn" to expectedEncounteredOn,
        "createdAt" to firstBp.createdAt,
        "updatedAt" to firstBp.createdAt,
        "deletedAt" to null
    )

    db_50.query("""SELECT * FROM "Encounter" """).use {
      assertThat(it.count).isEqualTo(1)
      it.moveToFirst()
      it.assertValues(expectedEncounterValues)
    }
  }

  @Test
  fun migration_from_49_to_50_create_different_encounters_per_facility() {
    //given
    val bloodPressureMeasurementTable = "BloodPressureMeasurement"
    val userUuid = UUID.fromString("8c1cfd1a-10bc-4aaf-a648-3d477527a444")
    val facility1Uuid = UUID.fromString("0086ba86-314c-49d9-8822-2563e53bed92")
    val facility2Uuid = UUID.fromString("02b8b03e-a4b2-41fe-8776-4f98d49196d7")
    val patientUuid = UUID.fromString("c4233170-feb2-3d34-8115-98fef5a68837")
    val timestamp = Instant.parse("2018-07-03T00:00:00Z")

    val bpAtFacility1 = testData.bloodPressureMeasurement(
        uuid = UUID.fromString("3eb96138-5270-419c-bcdf-86db95d0bc8b"),
        facilityUuid = facility1Uuid,
        patientUuid = patientUuid,
        createdAt = timestamp,
        updatedAt = timestamp,
        recordedAt = timestamp,
        userUuid = userUuid
    )
    val bpAtFacility2 = testData.bloodPressureMeasurement(
        uuid = UUID.fromString("a2455b0c-cf14-46c9-9f73-6e18972982b0"),
        facilityUuid = facility2Uuid,
        patientUuid = patientUuid,
        createdAt = timestamp,
        updatedAt = timestamp,
        recordedAt = timestamp,
        userUuid = userUuid
    )
    val testRecords = listOf(bpAtFacility1, bpAtFacility2)

    val db_49 = helper.createDatabase(version = 49)

    testRecords
        .map {
          mapOf(
              "uuid" to it.uuid,
              "systolic" to it.reading.systolic,
              "diastolic" to it.reading.diastolic,
              "syncStatus" to "DONE",
              "userUuid" to it.userUuid,
              "facilityUuid" to it.facilityUuid,
              "patientUuid" to it.patientUuid,
              "createdAt" to it.createdAt,
              "updatedAt" to it.updatedAt,
              "deletedAt" to it.deletedAt,
              "recordedAt" to it.recordedAt
          )
        }
        .forEach { db_49.insert(bloodPressureMeasurementTable, it) }

    //when
    val db_50 = helper.migrateTo(version = 50)

    //then
    val userClock = TestUserClock()

    val expectedDateForEncounters = timestamp.toLocalDateAtZone(userClock.zone)

    val expectedIdForFirstEncounter = generateEncounterUuid(
        facilityUuid = facility1Uuid,
        patientUuid = patientUuid,
        encounteredDate = expectedDateForEncounters
    )
    val expectedIdForSecondEncounter = generateEncounterUuid(
        facilityUuid = facility2Uuid,
        patientUuid = patientUuid,
        encounteredDate = expectedDateForEncounters
    )

    db_50.query("""SELECT * FROM '$bloodPressureMeasurementTable' """).use {
      assertThat(it.count).isEqualTo(2)
      assertThat(it.columnCount).isEqualTo(12)

      it.moveToFirst()
      assertThat(it.uuid("uuid")).isEqualTo(bpAtFacility1.uuid)
      assertThat(it.uuid("encounterUuid")).isEqualTo(expectedIdForFirstEncounter)

      it.moveToNext()
      assertThat(it.uuid("uuid")).isEqualTo(bpAtFacility2.uuid)
      assertThat(it.uuid("encounterUuid")).isEqualTo(expectedIdForSecondEncounter)
    }

    val expectedEncounterValuesForFirstEncounter = mapOf(
        "uuid" to expectedIdForFirstEncounter,
        "patientUuid" to patientUuid,
        "encounteredOn" to expectedDateForEncounters,
        "createdAt" to timestamp,
        "updatedAt" to timestamp,
        "deletedAt" to null
    )
    val expectedEncounterValuesForSecondEncounter = mapOf(
        "uuid" to expectedIdForSecondEncounter,
        "patientUuid" to patientUuid,
        "encounteredOn" to expectedDateForEncounters,
        "createdAt" to timestamp,
        "updatedAt" to timestamp,
        "deletedAt" to null
    )

    db_50.query("""SELECT * FROM "Encounter" """).use {
      assertThat(it.count).isEqualTo(2)

      it.moveToFirst()
      it.assertValues(expectedEncounterValuesForFirstEncounter)

      it.moveToNext()
      it.assertValues(expectedEncounterValuesForSecondEncounter)
    }
  }

  @Test
  fun migration_from_49_to_50_create_different_encounters_per_date() {
    //given
    val bloodPressureMeasurementTable = "BloodPressureMeasurement"
    val userUuid = UUID.fromString("8c1cfd1a-10bc-4aaf-a648-3d477527a444")
    val facilityUuid = UUID.fromString("0086ba86-314c-49d9-8822-2563e53bed92")
    val patientUuid = UUID.fromString("c4233170-feb2-3d34-8115-98fef5a68837")
    val timestampForFirstBp = Instant.parse("2018-07-01T00:00:00Z")
    val timestampForSecondBp = Instant.parse("2018-07-02T00:00:00Z")

    val firstBp = testData.bloodPressureMeasurement(
        uuid = UUID.fromString("3eb96138-5270-419c-bcdf-86db95d0bc8b"),
        facilityUuid = facilityUuid,
        patientUuid = patientUuid,
        createdAt = timestampForFirstBp,
        updatedAt = timestampForFirstBp,
        recordedAt = timestampForFirstBp,
        userUuid = userUuid
    )
    val secondBp = testData.bloodPressureMeasurement(
        uuid = UUID.fromString("a2455b0c-cf14-46c9-9f73-6e18972982b0"),
        facilityUuid = facilityUuid,
        patientUuid = patientUuid,
        createdAt = timestampForSecondBp,
        updatedAt = timestampForSecondBp,
        recordedAt = timestampForSecondBp,
        userUuid = userUuid
    )
    val testRecords = listOf(firstBp, secondBp)

    val db_49 = helper.createDatabase(version = 49)

    testRecords
        .map {
          mapOf(
              "uuid" to it.uuid,
              "systolic" to it.reading.systolic,
              "diastolic" to it.reading.diastolic,
              "syncStatus" to "DONE",
              "userUuid" to it.userUuid,
              "facilityUuid" to it.facilityUuid,
              "patientUuid" to it.patientUuid,
              "createdAt" to it.createdAt,
              "updatedAt" to it.updatedAt,
              "deletedAt" to it.deletedAt,
              "recordedAt" to it.recordedAt
          )
        }
        .forEach { db_49.insert(bloodPressureMeasurementTable, it) }

    //when
    val db_50 = helper.migrateTo(version = 50)

    //then
    val userClock = TestUserClock()

    val expectedDateForFirstEncounter = timestampForFirstBp.toLocalDateAtZone(userClock.zone)
    val expectedIdForFirstEncounter = generateEncounterUuid(
        facilityUuid = facilityUuid,
        patientUuid = patientUuid,
        encounteredDate = expectedDateForFirstEncounter
    )

    val expectedDateForSecondEncounter = timestampForSecondBp.toLocalDateAtZone(userClock.zone)
    val expectedIdForSecondEncounter = generateEncounterUuid(
        facilityUuid = facilityUuid,
        patientUuid = patientUuid,
        encounteredDate = expectedDateForSecondEncounter
    )

    db_50.query("""SELECT * FROM '$bloodPressureMeasurementTable' """).use {
      assertThat(it.count).isEqualTo(2)
      assertThat(it.columnCount).isEqualTo(12)

      it.moveToFirst()
      assertThat(it.uuid("uuid")).isEqualTo(firstBp.uuid)
      assertThat(it.uuid("encounterUuid")).isEqualTo(expectedIdForFirstEncounter)

      it.moveToNext()
      assertThat(it.uuid("uuid")).isEqualTo(secondBp.uuid)
      assertThat(it.uuid("encounterUuid")).isEqualTo(expectedIdForSecondEncounter)
    }

    val expectedEncounterValuesForFirstEncounter = mapOf(
        "uuid" to expectedIdForFirstEncounter,
        "patientUuid" to patientUuid,
        "encounteredOn" to expectedDateForFirstEncounter,
        "createdAt" to timestampForFirstBp,
        "updatedAt" to timestampForFirstBp,
        "deletedAt" to null
    )
    val expectedEncounterValuesForSecondEncounter = mapOf(
        "uuid" to expectedIdForSecondEncounter,
        "patientUuid" to patientUuid,
        "encounteredOn" to expectedDateForSecondEncounter,
        "createdAt" to timestampForSecondBp,
        "updatedAt" to timestampForSecondBp,
        "deletedAt" to null
    )

    db_50.query("""SELECT * FROM "Encounter" """).use {
      assertThat(it.count).isEqualTo(2)

      it.moveToFirst()
      it.assertValues(expectedEncounterValuesForFirstEncounter)

      it.moveToNext()
      it.assertValues(expectedEncounterValuesForSecondEncounter)
    }
  }

  @Test
  fun migration_from_49_to_50_create_different_encounters_per_patient() {
    //given
    val bloodPressureMeasurementTable = "BloodPressureMeasurement"
    val userUuid = UUID.fromString("8c1cfd1a-10bc-4aaf-a648-3d477527a444")
    val facilityUuid = UUID.fromString("0086ba86-314c-49d9-8822-2563e53bed92")
    val firstPatientUuid = UUID.fromString("c4233170-feb2-3d34-8115-98fef5a68837")
    val secondPatientUuid = UUID.fromString("cbce693e-7dfa-404d-9b75-04732986db3e")
    val timestamp = Instant.parse("2018-07-01T00:00:00Z")

    val bpForFirstPatient = testData.bloodPressureMeasurement(
        uuid = UUID.fromString("3eb96138-5270-419c-bcdf-86db95d0bc8b"),
        facilityUuid = facilityUuid,
        patientUuid = firstPatientUuid,
        createdAt = timestamp,
        updatedAt = timestamp,
        recordedAt = timestamp,
        userUuid = userUuid
    )
    val bpForSecondPatient = testData.bloodPressureMeasurement(
        uuid = UUID.fromString("a2455b0c-cf14-46c9-9f73-6e18972982b0"),
        facilityUuid = facilityUuid,
        patientUuid = secondPatientUuid,
        createdAt = timestamp,
        updatedAt = timestamp,
        recordedAt = timestamp,
        userUuid = userUuid
    )
    val testRecords = listOf(bpForFirstPatient, bpForSecondPatient)

    val db_49 = helper.createDatabase(version = 49)

    testRecords
        .map {
          mapOf(
              "uuid" to it.uuid,
              "systolic" to it.reading.systolic,
              "diastolic" to it.reading.diastolic,
              "syncStatus" to "DONE",
              "userUuid" to it.userUuid,
              "facilityUuid" to it.facilityUuid,
              "patientUuid" to it.patientUuid,
              "createdAt" to it.createdAt,
              "updatedAt" to it.updatedAt,
              "deletedAt" to it.deletedAt,
              "recordedAt" to it.recordedAt
          )
        }
        .forEach { db_49.insert(bloodPressureMeasurementTable, it) }

    //when
    val db_50 = helper.migrateTo(version = 50)

    //then
    val userClock = TestUserClock()

    val expectedDateForEncounters = timestamp.toLocalDateAtZone(userClock.zone)

    val expectedIdForFirstEncounter = generateEncounterUuid(
        facilityUuid = facilityUuid,
        patientUuid = firstPatientUuid,
        encounteredDate = expectedDateForEncounters
    )
    val expectedIdForSecondEncounter = generateEncounterUuid(
        facilityUuid = facilityUuid,
        patientUuid = secondPatientUuid,
        encounteredDate = expectedDateForEncounters
    )

    db_50.query("""SELECT * FROM '$bloodPressureMeasurementTable' """).use {
      assertThat(it.count).isEqualTo(2)
      assertThat(it.columnCount).isEqualTo(12)

      it.moveToFirst()
      assertThat(it.uuid("uuid")).isEqualTo(bpForFirstPatient.uuid)
      assertThat(it.uuid("encounterUuid")).isEqualTo(expectedIdForFirstEncounter)

      it.moveToNext()
      assertThat(it.uuid("uuid")).isEqualTo(bpForSecondPatient.uuid)
      assertThat(it.uuid("encounterUuid")).isEqualTo(expectedIdForSecondEncounter)
    }

    val expectedEncounterValuesForFirstEncounter = mapOf(
        "uuid" to expectedIdForFirstEncounter,
        "patientUuid" to firstPatientUuid,
        "encounteredOn" to expectedDateForEncounters,
        "createdAt" to timestamp,
        "updatedAt" to timestamp,
        "deletedAt" to null
    )
    val expectedEncounterValuesForSecondEncounter = mapOf(
        "uuid" to expectedIdForSecondEncounter,
        "patientUuid" to secondPatientUuid,
        "encounteredOn" to expectedDateForEncounters,
        "createdAt" to timestamp,
        "updatedAt" to timestamp,
        "deletedAt" to null
    )

    db_50.query("""SELECT * FROM "Encounter" """).use {
      assertThat(it.count).isEqualTo(2)

      it.moveToFirst()
      it.assertValues(expectedEncounterValuesForFirstEncounter)

      it.moveToNext()
      it.assertValues(expectedEncounterValuesForSecondEncounter)
    }
  }

  @Test
  fun migration_from_50_to_51_should_add_syncStatus_to_Encounter() {
    //given
    val db_50 = helper.createDatabase(version = 50)
    val encounterTable = "Encounter"

    db_50.assertColumns(tableName = encounterTable, expectedColumns = setOf(
        "uuid",
        "patientUuid",
        "encounteredOn",
        "createdAt",
        "updatedAt",
        "deletedAt"
    ))

    //when
    val db_51 = helper.migrateTo(version = 51)

    //then
    db_51.assertColumns(tableName = encounterTable, expectedColumns = setOf(
        "uuid",
        "patientUuid",
        "encounteredOn",
        "syncStatus",
        "createdAt",
        "updatedAt",
        "deletedAt"
    ))
  }

  @Test
  fun migration_from_51_to_52_should_set_the_deletedAt_to_null_for_encounters_with_deletedAt_set_to_string_null() {
    fun readEncounters(database: SupportSQLiteDatabase): Set<Pair<UUID, String?>> {
      return database.query(""" SELECT "uuid", "deletedAt" FROM "Encounter" """)
          .use { cursor ->
            generateSequence { cursor.takeIf { it.moveToNext() } }
                .map { it.uuid("uuid")!! to it.string("deletedAt") }
                .toSet()
          }
    }

    // given
    val db_51 = helper.createDatabase(51)

    val deletedEncounterUuid = UUID.fromString("e321da68-c015-4da4-8796-529c08c8bf4f")
    val deletedEncounterDeletedAt = Instant.parse("2018-01-01T00:00:01Z")

    val nullStringDeletedAtEncounterUuid = UUID.fromString("372e633c-e02d-403c-86e4-7a0cebfc5f21")
    val notDeletedEncounterUuid = UUID.fromString("92fcc816-93be-48ce-9fbd-679dbe56bf4c")

    val patientUuid = UUID.fromString("6d05b15c-f7e3-4159-a04c-21151b1bee07")

    with(db_51) {
      val encounterTable = "Encounter"

      insert(encounterTable, mapOf(
          "uuid" to deletedEncounterUuid,
          "patientUuid" to patientUuid,
          "encounteredOn" to LocalDate.parse("2018-01-01"),
          "syncStatus" to "DONE",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "deletedAt" to deletedEncounterDeletedAt
      ))

      insert(encounterTable, mapOf(
          "uuid" to nullStringDeletedAtEncounterUuid,
          "patientUuid" to patientUuid,
          "encounteredOn" to LocalDate.parse("2018-01-01"),
          "syncStatus" to "DONE",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "deletedAt" to "null"
      ))

      insert(encounterTable, mapOf(
          "uuid" to notDeletedEncounterUuid,
          "patientUuid" to patientUuid,
          "encounteredOn" to LocalDate.parse("2018-01-01"),
          "syncStatus" to "DONE",
          "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "updatedAt" to Instant.parse("2018-01-01T00:00:00Z"),
          "deletedAt" to null
      ))
    }

    assertThat(readEncounters(db_51))
        .containsExactly(
            deletedEncounterUuid to deletedEncounterDeletedAt.toString(),
            nullStringDeletedAtEncounterUuid to "null",
            notDeletedEncounterUuid to null
        )

    // when
    val db_52 = helper.migrateTo(52)

    // then
    assertThat(readEncounters(db_52))
        .containsExactly(
            deletedEncounterUuid to deletedEncounterDeletedAt.toString(),
            nullStringDeletedAtEncounterUuid to null,
            notDeletedEncounterUuid to null
        )
  }

  @Test
  fun migration_from_52_to_53_should_add_house_address_and_zone_columns() {
    val db_52 = helper.createDatabase(52)
    val selectAllPatientAddressesQuery = "SELECT * FROM PatientAddress"
    db_52.query(selectAllPatientAddressesQuery).use {
      assertThat(it.columnCount)
          .isEqualTo(8)
    }

    val db_53 = helper.migrateTo(53)
    db_53.query(selectAllPatientAddressesQuery).use {
      assertThat(it.columnCount)
          .isEqualTo(10)

      val columns = setOf("uuid", "streetAddress", "colonyOrVillage", "zone", "district", "state", "country", "createdAt", "updatedAt", "deletedAt")
      db_53.assertColumns("PatientAddress", columns)
    }
  }

  @Test
  fun migration_to_54_should_remove_the_encounterUuid_column_from_the_BloodPressureMeasurement_table() {
    // given
    val db_v53 = helper.createDatabase(53)
    db_v53.insert("BloodPressureMeasurement", mapOf(
        "uuid" to UUID.fromString("99e9a490-deaa-4eab-b3c4-f63f1566e76c"),
        "systolic" to 120,
        "diastolic" to 80,
        "syncStatus" to "DONE",
        "userUuid" to UUID.fromString("ae1515fe-b68e-47e1-ac3d-28b581e8c749"),
        "facilityUuid" to UUID.fromString("1b2ec11b-4bf0-4ca3-aebd-9c6ca618ea24"),
        "patientUuid" to UUID.fromString("1462d918-c778-42d9-bb8e-545682156530"),
        "encounterUuid" to UUID.fromString("aaffec4b-3aa9-4332-80f8-7718875c81ee"),
        "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
        "deletedAt" to null,
        "recordedAt" to Instant.parse("2017-12-31T00:00:00Z")
    ))
    db_v53.insert("BloodPressureMeasurement", mapOf(
        "uuid" to UUID.fromString("84618efe-7aef-4871-9309-9ff5d8fef764"),
        "systolic" to 140,
        "diastolic" to 90,
        "syncStatus" to "PENDING",
        "userUuid" to UUID.fromString("ae1515fe-b68e-47e1-ac3d-28b581e8c749"),
        "facilityUuid" to UUID.fromString("1b2ec11b-4bf0-4ca3-aebd-9c6ca618ea24"),
        "patientUuid" to UUID.fromString("dfb647d5-c780-4324-bca6-4d07a01bbcb9"),
        "encounterUuid" to UUID.fromString("66d01c35-0996-492f-9bf4-f9817f73f968"),
        "createdAt" to Instant.parse("2018-01-02T00:00:00Z"),
        "updatedAt" to Instant.parse("2018-01-02T00:00:01Z"),
        "deletedAt" to Instant.parse("2018-01-02T00:00:02Z"),
        "recordedAt" to Instant.parse("2018-01-01T00:00:00Z")
    ))

    // when
    val db_v54 = helper.migrateTo(54)

    // then
    db_v54.assertColumns(
        "BloodPressureMeasurement",
        setOf(
            "uuid",
            "systolic",
            "diastolic",
            "syncStatus",
            "userUuid",
            "facilityUuid",
            "patientUuid",
            "createdAt",
            "updatedAt",
            "deletedAt",
            "recordedAt"
        )
    )
    db_v54.query(""" SELECT * from "BloodPressureMeasurement" """).use { cursor ->

      with(cursor) {
        assertThat(count).isEqualTo(2)

        moveToNext()
        assertValues(mapOf(
            "uuid" to UUID.fromString("99e9a490-deaa-4eab-b3c4-f63f1566e76c"),
            "systolic" to 120,
            "diastolic" to 80,
            "syncStatus" to "DONE",
            "userUuid" to UUID.fromString("ae1515fe-b68e-47e1-ac3d-28b581e8c749"),
            "facilityUuid" to UUID.fromString("1b2ec11b-4bf0-4ca3-aebd-9c6ca618ea24"),
            "patientUuid" to UUID.fromString("1462d918-c778-42d9-bb8e-545682156530"),
            "createdAt" to Instant.parse("2018-01-01T00:00:00Z"),
            "updatedAt" to Instant.parse("2018-01-01T00:00:01Z"),
            "deletedAt" to null,
            "recordedAt" to Instant.parse("2017-12-31T00:00:00Z")
        ))

        moveToNext()
        assertValues(mapOf(
            "uuid" to UUID.fromString("84618efe-7aef-4871-9309-9ff5d8fef764"),
            "systolic" to 140,
            "diastolic" to 90,
            "syncStatus" to "PENDING",
            "userUuid" to UUID.fromString("ae1515fe-b68e-47e1-ac3d-28b581e8c749"),
            "facilityUuid" to UUID.fromString("1b2ec11b-4bf0-4ca3-aebd-9c6ca618ea24"),
            "patientUuid" to UUID.fromString("dfb647d5-c780-4324-bca6-4d07a01bbcb9"),
            "createdAt" to Instant.parse("2018-01-02T00:00:00Z"),
            "updatedAt" to Instant.parse("2018-01-02T00:00:01Z"),
            "deletedAt" to Instant.parse("2018-01-02T00:00:02Z"),
            "recordedAt" to Instant.parse("2018-01-01T00:00:00Z")
        ))
      }
    }
  }

  @Test
  fun migration_to_54_should_drop_the_Encounter_table() {
    // given
    val db_v53 = helper.createDatabase(53)
    db_v53.assertTableExists("Encounter")

    // when
    val db_v54 = helper.migrateTo(54)

    // then
    db_v54.assertTableDoesNotExist("Encounter")
  }
}
