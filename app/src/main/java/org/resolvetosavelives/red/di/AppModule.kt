package org.resolvetosavelives.red.di

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.os.Vibrator
import dagger.Module
import dagger.Provides
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.qrscan.QrDaggerModule

@Module(includes = [QrDaggerModule::class])
class AppModule(private val appContext: Application) {

  @Provides
  fun appDatabase(): AppDatabase {
    val databaseName = appContext.getString(R.string.app_name)
    return Room.databaseBuilder(appContext, AppDatabase::class.java, databaseName).build()
  }

  @Provides
  fun vibrator(): Vibrator {
    return appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
  }
}
