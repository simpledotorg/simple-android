package org.resolvetosavelives.red

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.resolvetosavelives.red.newentry.search.Patient
import org.resolvetosavelives.red.newentry.search.PatientDao
import org.resolvetosavelives.red.util.RoomGenderTypeConverter

@Database(entities = [Patient::class], version = 1, exportSchema = false)
@TypeConverters(RoomGenderTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun patientDao(): PatientDao
}
