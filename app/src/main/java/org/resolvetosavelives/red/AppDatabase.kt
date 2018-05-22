package org.resolvetosavelives.red

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.resolvetosavelives.red.newentry.search.Gender
import org.resolvetosavelives.red.newentry.search.Patient
import org.resolvetosavelives.red.newentry.search.PatientAddress
import org.resolvetosavelives.red.newentry.search.PatientAndPhoneNumberJoin
import org.resolvetosavelives.red.newentry.search.PatientDao
import org.resolvetosavelives.red.newentry.search.PatientPhoneNumber
import org.resolvetosavelives.red.newentry.search.PatientPhoneNumberType
import org.resolvetosavelives.red.newentry.search.PatientStatus
import org.resolvetosavelives.red.util.InstantRoomTypeConverter
import org.resolvetosavelives.red.util.LocalDateRoomTypeConverter

@Database(
    entities = [Patient::class, PatientAddress::class, PatientPhoneNumber::class, PatientAndPhoneNumberJoin::class],
    version = 1,
    exportSchema = false)
@TypeConverters(
    Gender.RoomTypeConverter::class,
    PatientPhoneNumberType.RoomTypeConverter::class,
    PatientStatus.RoomTypeConverter::class,
    InstantRoomTypeConverter::class,
    LocalDateRoomTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun patientDao(): PatientDao
}
