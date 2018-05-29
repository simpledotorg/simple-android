package org.resolvetosavelives.red

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.resolvetosavelives.red.newentry.search.Gender
import org.resolvetosavelives.red.newentry.search.Patient
import org.resolvetosavelives.red.newentry.search.PatientAddress
import org.resolvetosavelives.red.newentry.search.PatientPhoneNumber
import org.resolvetosavelives.red.newentry.search.PatientPhoneNumberType
import org.resolvetosavelives.red.newentry.search.PatientStatus
import org.resolvetosavelives.red.newentry.search.PatientWithAddress
import org.resolvetosavelives.red.newentry.search.PatientWithPhoneNumber
import org.resolvetosavelives.red.newentry.search.SyncStatus
import org.resolvetosavelives.red.util.InstantRoomTypeConverter
import org.resolvetosavelives.red.util.LocalDateRoomTypeConverter

@Database(
    entities = [Patient::class, PatientAddress::class, PatientPhoneNumber::class, PatientWithPhoneNumber::class],
    version = 1,
    exportSchema = false)
@TypeConverters(
    Gender.RoomTypeConverter::class,
    PatientPhoneNumberType.RoomTypeConverter::class,
    PatientStatus.RoomTypeConverter::class,
    SyncStatus.RoomTypeConvert::class,
    InstantRoomTypeConverter::class,
    LocalDateRoomTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun patientDao(): Patient.RoomDao

  abstract fun addressDao(): PatientAddress.RoomDao

  abstract fun phoneNumberDao(): PatientPhoneNumber.RoomDao

  abstract fun patientWithAddressDao(): PatientWithAddress.RoomDao
}
