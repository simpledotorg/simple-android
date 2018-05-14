package org.resolvetosavelives.red

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import org.resolvetosavelives.red.newentry.search.Patient
import org.resolvetosavelives.red.newentry.search.PatientDao

@Database(entities = [Patient::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

  abstract fun patientDao(): PatientDao
}
