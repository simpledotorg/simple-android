package org.resolvetosavelives.red.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.os.Vibrator
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.qrscan.QrDaggerModule
import org.resolvetosavelives.red.sync.SyncModule

// TODO: Should this class be named as AppDaggerModule, just like QrDaggerModule?

@Module(includes = [QrDaggerModule::class, SyncModule::class, NetworkModule::class, StorageModule::class])
open class AppModule(private val appContext: Application, private val databaseName: String = "red-db") {

  @Provides
  fun appContext(): Application {
    return appContext
  }

  // TODO: Move to StorageModule.
  @Provides
  open fun appDatabase(appContext: Application): AppDatabase {
    return Room.databaseBuilder(appContext, AppDatabase::class.java, databaseName).build()
  }

  @Provides
  fun vibrator() = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

  @Provides
  fun workManager() = WorkManager.getInstance()!!
}
