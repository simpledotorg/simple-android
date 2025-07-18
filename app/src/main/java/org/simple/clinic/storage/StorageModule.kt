package org.simple.clinic.storage

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.requery.android.database.sqlite.SQLiteGlobal
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.simple.clinic.AppDatabase
import org.simple.clinic.DATABASE_NAME
import org.simple.clinic.di.AppScope
import org.simple.clinic.questionnaire.component.BaseComponentData
import org.simple.clinic.storage.migrations.RoomMigrationsModule
import org.simple.clinic.storage.text.TextRecord
import org.simple.clinic.storage.text.TextStoreModule
import org.simple.clinic.user.User
import org.simple.clinic.util.MinimumMemoryChecker
import org.simple.clinic.util.ThreadPools

@Module(includes = [
  RoomMigrationsModule::class,
  SharedPreferencesModule::class,
  TextStoreModule::class,
  SqliteModule::class,
  EncryptionModule::class
])
class StorageModule {

  @Provides
  @AppScope
  fun appDatabase(
      appContext: Application,
      migrations: List<@JvmSuppressWildcards Migration>,
      moshi: Moshi,
      databaseEncryptor: DatabaseEncryptor,
      minimumMemoryChecker: MinimumMemoryChecker,
      factory: SupportSQLiteOpenHelper.Factory
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

    val helperFactory = if (minimumMemoryChecker.hasMinimumRequiredMemory()) {
      val passphrase = databaseEncryptor.passphrase
      SupportOpenHelperFactory(passphrase)
    } else {
      factory
    }

    return Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
        .openHelperFactory(helperFactory)
        .addMigrations(*migrations.toTypedArray())
        .addTypeConverter(BaseComponentData.RoomTypeConverter(moshi))
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
