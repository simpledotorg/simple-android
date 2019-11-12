package org.simple.clinic.storage

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Module
import dagger.Provides
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.di.AppSqliteOpenHelperFactory
import org.simple.clinic.storage.files.AndroidFileStorage
import org.simple.clinic.storage.files.FileOperations
import org.simple.clinic.storage.files.FileStorage

@Module(includes = [RoomMigrationsModule::class])
open class StorageModule(
    private val databaseName: String = "red-db",
    private val runDatabaseQueriesOnMainThread: Boolean = false
) {

  @Provides
  @AppScope
  open fun appDatabase(
      appContext: Application,
      factory: SupportSQLiteOpenHelper.Factory,
      migrations: List<@JvmSuppressWildcards Migration>
  ): AppDatabase {
    return Room.databaseBuilder(appContext, AppDatabase::class.java, databaseName)
        .openHelperFactory(factory)
        .apply {
          if (runDatabaseQueriesOnMainThread) {
            allowMainThreadQueries()
          }
        }
        .addMigrations(*migrations.toTypedArray())
        .build()
  }

  @Provides
  fun rxSharedPreferences(appContext: Application): RxSharedPreferences {
    val preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
    return RxSharedPreferences.create(preferences)
  }

  @Provides
  fun sharedPreferences(appContext: Application): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(appContext)
  }

  @Provides
  open fun sqliteOpenHelperFactory(): SupportSQLiteOpenHelper.Factory = AppSqliteOpenHelperFactory()

  @Provides
  fun fileStorage(application: Application, fileOperations: FileOperations): FileStorage {
    return AndroidFileStorage(application, fileOperations)
  }
}
