@file:Suppress("ClassName")

package org.simple.clinic

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientFuzzySearch
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.nameToSearchableForm
import org.simple.clinic.user.LoggedInUser
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.InstantRoomTypeConverter
import org.simple.clinic.util.LocalDateRoomTypeConverter
import org.simple.clinic.util.UuidListRoomTypeConverter
import org.simple.clinic.util.UuidRoomTypeConverter

@Database(
    entities = [
      Patient::class,
      PatientAddress::class,
      PatientPhoneNumber::class,
      BloodPressureMeasurement::class,
      PrescribedDrug::class,
      Facility::class,
      LoggedInUser::class],
    version = 7,
    exportSchema = true)
@TypeConverters(
    Gender.RoomTypeConverter::class,
    PatientPhoneNumberType.RoomTypeConverter::class,
    PatientStatus.RoomTypeConverter::class,
    SyncStatus.RoomTypeConverter::class,
    UserStatus.RoomTypeConverter::class,
    InstantRoomTypeConverter::class,
    LocalDateRoomTypeConverter::class,
    UuidRoomTypeConverter::class,
    UuidListRoomTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

  private val patientFuzzyPatientSearchDao by lazy {
    PatientFuzzySearch.PatientFuzzySearchDaoImpl(openHelper, patientSearchDao())
  }

  abstract fun patientDao(): Patient.RoomDao

  abstract fun addressDao(): PatientAddress.RoomDao

  abstract fun phoneNumberDao(): PatientPhoneNumber.RoomDao

  abstract fun patientSearchDao(): PatientSearchResult.RoomDao

  abstract fun bloodPressureDao(): BloodPressureMeasurement.RoomDao

  abstract fun prescriptionDao(): PrescribedDrug.RoomDao

  abstract fun facilityDao(): Facility.RoomDao

  abstract fun userDao(): LoggedInUser.RoomDao

  fun fuzzyPatientSearchDao(): PatientFuzzySearch.PatientFuzzySearchDao = patientFuzzyPatientSearchDao

  class Migration_3_4 : Migration(3, 4) {

    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL("""
        CREATE TABLE IF NOT EXISTS `LoggedInUser` (
          `uuid` TEXT NOT NULL,
          `fullName` TEXT NOT NULL,
          `phoneNumber` TEXT NOT NULL,
          `pinDigest` TEXT NOT NULL,
          `facilityUuid` TEXT NOT NULL,
          `status` TEXT NOT NULL,
          `createdAt` TEXT NOT NULL,
          `updatedAt` TEXT NOT NULL,
          PRIMARY KEY(`uuid`)
        )
        """)
    }
  }

  class Migration_4_5 : Migration(4, 5) {

    override fun migrate(database: SupportSQLiteDatabase) {
      // Update local searchable name in the Patient table to strip out the newly added characters
      database.inTransaction {
        compileStatement("""UPDATE "Patient" SET "searchableName"=? WHERE "uuid"=?""")
            .use { statement ->
              query("""SELECT "uuid","fullName" FROM "Patient"""")
                  .use { cursor ->
                    val uuidIndex = cursor.getColumnIndex("uuid")
                    val fullNameIndex = cursor.getColumnIndex("fullName")

                    generateSequence { cursor.takeIf { it.moveToNext() } }
                        .map { it.getString(uuidIndex) to nameToSearchableForm(it.getString(fullNameIndex)) }
                        .forEach { (uuid, searchableName) ->
                          statement.bindString(1, searchableName)
                          statement.bindString(2, uuid)
                          statement.executeUpdateDelete()
                        }
                  }
            }
      }
    }
  }

  class Migration_5_6 : Migration(5, 6) {

    override fun migrate(database: SupportSQLiteDatabase) {
      database.inTransaction {
        // We need to manually create this table because it's a virtual table and Room doesn't support virtual tables (yet!)
        PatientFuzzySearch.createTable(this)
        execSQL("""INSERT INTO "PatientFuzzySearch"("rowid","word") SELECT "rowid","searchableName" FROM "Patient"""")
      }
    }
  }

  /**
   * Renames LoggedInUser.facilityUuid column to LoggedInUser.facilityUuids.
   * */
  class Migration_6_7 : Migration(6, 7) {

    override fun migrate(database: SupportSQLiteDatabase) {
      database.inTransaction {
        database.execSQL("ALTER TABLE `LoggedInUser` RENAME TO `LoggedInUser_v6`")
        database.execSQL("""
        CREATE TABLE IF NOT EXISTS `LoggedInUser` (
        `uuid` TEXT NOT NULL,
        `fullName` TEXT NOT NULL,
        `phoneNumber` TEXT NOT NULL,
        `pinDigest` TEXT NOT NULL,
        `facilityUuids` TEXT NOT NULL,
        `status` TEXT NOT NULL,
        `createdAt` TEXT NOT NULL,
        `updatedAt` TEXT NOT NULL,
        PRIMARY KEY(`uuid`));
        """)
        database.execSQL("""
        INSERT INTO `LoggedInUser`(`uuid`, `fullName`, `phoneNumber`, `pinDigest`, `facilityUuids`, `status`, `createdAt`, `updatedAt`)
        SELECT `uuid`, `fullName`, `phoneNumber`, `pinDigest`, `facilityUuid`, `status`, `createdAt`, `updatedAt`
        FROM `LoggedInUser_v6`
        """)
        database.execSQL("DROP TABLE `LoggedInUser_v6`")
      }
    }
  }
}

private fun SupportSQLiteDatabase.inTransaction(block: SupportSQLiteDatabase.() -> Unit) {
  beginTransaction()
  try {
    block.invoke(this)
    setTransactionSuccessful()
  } finally {
    endTransaction()
  }
}
