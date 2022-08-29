package org.simple.clinic.di

import android.app.Application
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientModule
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.storage.SharedPreferencesModule
import org.simple.clinic.storage.migrations.RoomMigrationsModule
import org.simple.clinic.storage.text.TextRecord
import org.simple.clinic.storage.text.TextStoreModule
import org.simple.clinic.summary.PatientSummaryModule
import org.simple.clinic.user.User

@Module(includes = [
  RoomMigrationsModule::class,
  SharedPreferencesModule::class,
  PatientModule::class,
  PatientSummaryModule::class,
  TextStoreModule::class
])
class TestStorageModule {

  @Provides
  fun sqliteOpenHelperFactory(): SupportSQLiteOpenHelper.Factory = AppSqliteOpenHelperFactory(inMemory = false)

  @Provides
  fun appDatabase(
      appContext: Application,
      factory: SupportSQLiteOpenHelper.Factory
  ): AppDatabase {

    return Room.databaseBuilder(appContext, AppDatabase::class.java, "test-db")
        .openHelperFactory(factory)
        .apply { allowMainThreadQueries() }
        .build()
  }

  @Provides
  fun userDao(appDatabase: AppDatabase): User.RoomDao {
    return appDatabase.userDao()
  }

  @Provides
  fun provideTextStoreDao(appDatabase: AppDatabase): TextRecord.RoomDao = appDatabase.textRecordDao()

  @Provides
  fun providePatientDao(appDatabase: AppDatabase): Patient.RoomDao = appDatabase.patientDao()

  @Provides
  fun providePatientSearchDao(appDatabase: AppDatabase): PatientSearchResult.RoomDao = appDatabase.patientSearchDao()
}
