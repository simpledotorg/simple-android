package org.simple.clinic.di

import androidx.sqlite.db.SupportSQLiteOpenHelper
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteDatabaseConfiguration

/**
 * Instance of [SupportSQLiteOpenHelper.Factory] that does the following:
 * - Creates an instance of [RequerySQLiteOpenHelperFactory] internally
 * - Has a flag to set whether it should be an in-memory database
 *
 * **Note:** There is a reason why the configuration for in-memory databases was moved to this class
 * instead of via Room's database builder. `sqlite-android`'s default in-memory implementation ignores
 * all dynamic extensions in the configuration and hence, testing the databases while using the extension
 * is a lot more problematic.
 *
 * However, by making it a normal database in Room, and overriding the configuration provided in this
 * class, we can make it an in-memory database with the extension loaded
 **/
class AppSqliteOpenHelperFactory(inMemory: Boolean = false) : SupportSQLiteOpenHelper.Factory {

  private val factory: RequerySQLiteOpenHelperFactory

  init {
    factory = RequerySQLiteOpenHelperFactory(createConfigurations(inMemory))
  }

  private fun createConfigurations(inMemory: Boolean) =
      listOf(RequerySQLiteOpenHelperFactory.ConfigurationOptions { config ->
        if (inMemory) {
          SQLiteDatabaseConfiguration(SQLiteDatabaseConfiguration.MEMORY_DB_PATH, SQLiteDatabase.CREATE_IF_NECESSARY)
        } else {
          config
        }
      })

  override fun create(configuration: SupportSQLiteOpenHelper.Configuration) = factory.create(configuration)!!
}
