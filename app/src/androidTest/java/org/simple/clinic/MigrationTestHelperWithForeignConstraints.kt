package org.simple.clinic

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.test.InstrumentationRegistry
import org.simple.clinic.di.AppSqliteOpenHelperFactory

private const val TEST_DB_NAME = "migration-test"

class MigrationTestHelperWithForeignConstraints : MigrationTestHelper(
    InstrumentationRegistry.getInstrumentation(),
    AppDatabase::class.java.canonicalName,
    AppSqliteOpenHelperFactory()
) {

  lateinit var migrations: List<Migration>

  fun createDatabase(version: Int): SupportSQLiteDatabase {
    val db = super.createDatabase(TEST_DB_NAME, version)
    db.setForeignKeyConstraintsEnabled(true)
    return db
  }

  fun migrateTo(version: Int): SupportSQLiteDatabase {
    return super.runMigrationsAndValidate(TEST_DB_NAME, version, true, *migrations.toTypedArray())
  }

  @Deprecated(message = "Use migrateTo() instead", replaceWith = ReplaceWith("helper.migrateTo(version, *migrations)"))
  override fun runMigrationsAndValidate(
      name: String?,
      version: Int,
      validateDroppedTables: Boolean,
      vararg migrations: Migration?
  ): SupportSQLiteDatabase {
    return super.runMigrationsAndValidate(name, version, validateDroppedTables, *migrations)
  }
}
