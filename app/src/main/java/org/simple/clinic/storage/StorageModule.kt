package org.simple.clinic.storage

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.requery.android.database.sqlite.SQLiteGlobal
import org.simple.clinic.AppDatabase
import org.simple.clinic.di.AppScope
import org.simple.clinic.monthlyReports.questionnaire.QuestionnaireLayout
import org.simple.clinic.storage.migrations.RoomMigrationsModule
import org.simple.clinic.storage.text.TextRecord
import org.simple.clinic.storage.text.TextStoreModule
import org.simple.clinic.user.User
import org.simple.clinic.util.ThreadPools

@Module(includes = [
  RoomMigrationsModule::class,
  SharedPreferencesModule::class,
  TextStoreModule::class,
  SqliteModule::class
])
class StorageModule {

  @Provides
  @AppScope
  fun appDatabase(
      appContext: Application,
      factory: SupportSQLiteOpenHelper.Factory,
      migrations: List<@JvmSuppressWildcards Migration>,
      moshi: Moshi
  ): AppDatabase {
    // Don't occupy all connections with Room threads since there are
    // non-Room accesses of the database which SQLite itself might do
    // internally.
    val sqliteThreadPoolCount = SQLiteGlobal.getWALConnectionPoolSize() / 2
    val queryExecutor = ThreadPools.create(
        corePoolSize = sqliteThreadPoolCount,
        maxPoolSize = sqliteThreadPoolCount,
        threadPrefix = "room-query"
    )

    return Room.databaseBuilder(appContext, AppDatabase::class.java, "red-db")
        .openHelperFactory(factory)
        .addMigrations(*migrations.toTypedArray())
        .addTypeConverter(QuestionnaireLayout.RoomTypeConverter(moshi))
        .setQueryExecutor(queryExecutor)
        .build()
  }

  @Provides
  fun userDao(appDatabase: AppDatabase): User.RoomDao {
    return appDatabase.userDao()
  }

  @Provides
  fun provideTextStoreDao(appDatabase: AppDatabase): TextRecord.RoomDao = appDatabase.textRecordDao()
}
