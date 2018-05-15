package org.resolvetosavelives.red.di

import android.app.Application
import android.arch.persistence.room.Room
import dagger.Module
import dagger.Provides
import org.resolvetosavelives.red.AppDatabase
import org.resolvetosavelives.red.R

@Module
class AppModule(private val appContext: Application) {

  @Provides
  fun appDatabase(): AppDatabase {
    val databaseName = appContext.getString(R.string.app_name)
    return Room.databaseBuilder(appContext, AppDatabase::class.java, databaseName).build()
  }
}
