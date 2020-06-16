package org.simple.clinic.storage

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteOpenHelper
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.di.AppSqliteOpenHelperFactory
import org.simple.clinic.storage.migrations.RoomMigrationsModule
import org.simple.clinic.storage.text.TextRecord
import org.simple.clinic.storage.text.TextStoreModule
import org.simple.clinic.user.User

@Module(includes = [
  RoomMigrationsModule::class,
  SharedPreferencesModule::class,
  TextStoreModule::class
])
class StorageModule {

  @Provides
  @AppScope
  fun appDatabase(
      appContext: Application,
      factory: SupportSQLiteOpenHelper.Factory,
      migrations: List<@JvmSuppressWildcards Migration>
  ): AppDatabase {
    return Room.databaseBuilder(appContext, AppDatabase::class.java, "red-db")
        .openHelperFactory(factory)
        .addMigrations(*migrations.toTypedArray())
        .build()
  }

  @Provides
  fun sqliteOpenHelperFactory(): SupportSQLiteOpenHelper.Factory = AppSqliteOpenHelperFactory()

  @Provides
  fun userDao(appDatabase: AppDatabase): User.RoomDao {
    return appDatabase.userDao()
  }

  @Provides
  fun provideTextStoreDao(appDatabase: AppDatabase): TextRecord.RoomDao = appDatabase.textRecordDao()
}
