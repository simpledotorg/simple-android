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
import org.simple.clinic.storage.files.FileStorage
import org.simple.clinic.storage.migrations.RoomMigrationsModule
import org.simple.clinic.storage.text.LocalFileTextStore
import org.simple.clinic.storage.text.TextStore
import org.simple.clinic.user.User
import javax.inject.Named

@Module(includes = [
  RoomMigrationsModule::class,
  SharedPreferencesModule::class,
  FileStorageModule::class
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
  fun textStore(
      fileStorage: FileStorage,
      @Named("reports_file_path") reportsFilePath: String,
      @Named("help_file_path") helpFilePath: String
  ): TextStore {
    return LocalFileTextStore(
        fileStorage = fileStorage,
        keysToFilePath = mapOf(
            "reports" to reportsFilePath,
            "help" to helpFilePath
        )
    )
  }
}
