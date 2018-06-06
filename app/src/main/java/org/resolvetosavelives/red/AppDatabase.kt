package org.resolvetosavelives.red

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.resolvetosavelives.red.bp.BloodPressureMeasurement
import org.resolvetosavelives.red.search.Gender
import org.resolvetosavelives.red.search.Patient
import org.resolvetosavelives.red.search.PatientAddress
import org.resolvetosavelives.red.search.PatientPhoneNumber
import org.resolvetosavelives.red.search.PatientPhoneNumberType
import org.resolvetosavelives.red.search.PatientStatus
import org.resolvetosavelives.red.search.PatientWithAddressAndPhone
import org.resolvetosavelives.red.search.SyncStatus
import org.resolvetosavelives.red.util.InstantRoomTypeConverter
import org.resolvetosavelives.red.util.LocalDateRoomTypeConverter
import org.resolvetosavelives.red.util.UuidRoomTypeConverter

@Database(
    entities = [
      Patient::class,
      PatientAddress::class,
      PatientPhoneNumber::class,
      BloodPressureMeasurement::class],
    version = 1,
    exportSchema = false)
@TypeConverters(
    Gender.RoomTypeConverter::class,
    PatientPhoneNumberType.RoomTypeConverter::class,
    PatientStatus.RoomTypeConverter::class,
    SyncStatus.RoomTypeConvert::class,
    InstantRoomTypeConverter::class,
    LocalDateRoomTypeConverter::class,
    UuidRoomTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun patientDao(): Patient.RoomDao

  abstract fun addressDao(): PatientAddress.RoomDao

  abstract fun phoneNumberDao(): PatientPhoneNumber.RoomDao

  abstract fun patientAddressPhoneDao(): PatientWithAddressAndPhone.RoomDao

  abstract fun bloodPressureDao(): BloodPressureMeasurement.RoomDao
}
