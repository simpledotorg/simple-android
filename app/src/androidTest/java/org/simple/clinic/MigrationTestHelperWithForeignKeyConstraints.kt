package org.simple.clinic

import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.InstrumentationRegistry
import org.simple.clinic.di.AppSqliteOpenHelperFactory

private const val TEST_DB_NAME = "migration-test"

class MigrationTestHelperWithForeignKeyConstraints : MigrationTestHelper(
    InstrumentationRegistry.getInstrumentation(),
    AppDatabase::class.java.canonicalName,
    AppSqliteOpenHelperFactory()
) {

  lateinit var migrations: List<Migration>

  fun createDatabase(version: Int): SupportSQLiteDatabase {
    return super.createDatabase(TEST_DB_NAME, version).apply {
      setForeignKeyConstraintsEnabled(true)
    }
  }

  fun migrateTo(version: Int): SupportSQLiteDatabase {
    val validateDroppedTables = true
    return super.runMigrationsAndValidate(
        TEST_DB_NAME,
        version,
        validateDroppedTables,
        *migrations.toTypedArray()
    ).apply {
      setForeignKeyConstraintsEnabled(true)
    }
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
