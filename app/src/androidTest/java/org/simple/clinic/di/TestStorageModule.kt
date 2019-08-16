package org.simple.clinic.di

import android.app.Application
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.simple.clinic.AppDatabase
import org.simple.clinic.storage.StorageModule

/*
* We have moved the in-memory database configuration to the sqlite openhelper factory
* but we still have to provide a non-empty name for Room, otherwise it complains.
*/
class TestStorageModule : StorageModule(databaseName = "ignored", runDatabaseQueriesOnMainThread = true) {
  override fun sqliteOpenHelperFactory() = AppSqliteOpenHelperFactory(inMemory = true)

  override fun appDatabase(appContext: Application, factory: SupportSQLiteOpenHelper.Factory, migrations: ArrayList<Migration>): AppDatabase {
    return super.appDatabase(appContext, factory, migrations)
        .apply { openHelper.writableDatabase.setForeignKeyConstraintsEnabled(true) }
  }
}
