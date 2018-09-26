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
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.simple.clinic.AppDatabase
import org.simple.clinic.login.LoginModule
import org.simple.clinic.patient.PatientFuzzySearch
import org.simple.clinic.qrscan.QrModule
import org.simple.clinic.registration.RegistrationModule
import org.simple.clinic.storage.Migration_10_11
import org.simple.clinic.storage.Migration_11_12
import org.simple.clinic.storage.Migration_12_13
import org.simple.clinic.storage.Migration_13_14
import org.simple.clinic.storage.Migration_3_4
import org.simple.clinic.storage.Migration_4_5
import org.simple.clinic.storage.Migration_5_6
import org.simple.clinic.storage.Migration_6_7
import org.simple.clinic.storage.Migration_7_8
import org.simple.clinic.storage.Migration_8_9
import org.simple.clinic.storage.Migration_9_10
import org.simple.clinic.storage.StorageModule
import org.simple.clinic.sync.SyncModule
import org.threeten.bp.Clock
import javax.inject.Named

@Module(includes = [
  QrModule::class,
  SyncModule::class,
  NetworkModule::class,
  StorageModule::class,
  LoginModule::class,
  RegistrationModule::class])
open class AppModule(
    private val appContext: Application,
    private val databaseName: String = "red-db",
    private val runDatabaseQueriesOnMainThread: Boolean = false
) {

  @Provides
  fun appContext(): Application {
    return appContext
  }

  // TODO: move to StorageModule.
  @Provides
  @AppScope
  fun appDatabase(appContext: Application, factory: SupportSQLiteOpenHelper.Factory): AppDatabase {
    return Room.databaseBuilder(appContext, AppDatabase::class.java, databaseName)
        .openHelperFactory(factory)
        .apply {
          if (runDatabaseQueriesOnMainThread) {
            allowMainThreadQueries()
          }
        }
        .addCallback(object : RoomDatabase.Callback() {
          // We need to create it here on a fresh install because we can't
          // define an Entity for a virtual table and Room will never create it.
          override fun onCreate(db: SupportSQLiteDatabase) {
            PatientFuzzySearch.createTable(db)
          }
        })
        .addMigrations(
            Migration_3_4(),
            Migration_4_5(),
            Migration_5_6(),
            Migration_6_7(),
            Migration_7_8(),
            Migration_8_9(),
            Migration_9_10(),
            Migration_10_11(),
            Migration_11_12(),
            Migration_12_13(),
            Migration_13_14())
        .build()
  }

  @Provides
  fun vibrator() = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

  @Provides
  fun workManager() = WorkManager.getInstance()!!

  @Provides
  @AppScope
  open fun clock() = Clock.systemUTC()!!

  @Provides
  @Named("io")
  fun ioScheduler(): Scheduler = Schedulers.io()
}
