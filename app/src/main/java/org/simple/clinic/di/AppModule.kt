package org.simple.clinic.di

import android.app.Application
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.os.Vibrator
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.login.LoginModule
import org.simple.clinic.qrscan.QrModule
import org.simple.clinic.registration.RegistrationModule
import org.simple.clinic.sync.SyncModule

@Module(includes = [QrModule::class, SyncModule::class, NetworkModule::class, StorageModule::class, LoginModule::class, RegistrationModule::class])
class AppModule(private val appContext: Application, private val databaseName: String = "red-db") {

  @Provides
  fun appContext(): Application {
    return appContext
  }

  // TODO: Move to StorageModule.
  @Provides
  @AppScope
  fun appDatabase(appContext: Application, factory: SupportSQLiteOpenHelper.Factory): AppDatabase {
    return Room.databaseBuilder(appContext, AppDatabase::class.java, databaseName)
        .openHelperFactory(factory)
        .addCallback(object : RoomDatabase.Callback() {

          // We need to create it here on a fresh install because we can't define an Entity for a virtual
          // table and Room will never create it
          override fun onCreate(db: SupportSQLiteDatabase) {
            AppDatabase.createPatientFuzzySearchTable(db)
          }
        })
        .addMigrations(
            AppDatabase.Migration_3_4(),
            AppDatabase.Migration_4_5(),
            AppDatabase.Migration_5_6()
        )
        .build()
  }

  @Provides
  fun vibrator() = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

  @Provides
  fun workManager() = WorkManager.getInstance()!!
}
