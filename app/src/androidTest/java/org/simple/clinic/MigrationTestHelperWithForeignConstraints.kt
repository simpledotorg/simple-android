package org.simple.clinic

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import android.arch.persistence.room.testing.MigrationTestHelper
import android.support.test.InstrumentationRegistry
import org.simple.clinic.di.AppSqliteOpenHelperFactory

private const val TEST_DB_NAME = "migration-test"

class MigrationTestHelperWithForeignConstraints : MigrationTestHelper(
    InstrumentationRegistry.getInstrumentation(),
    AppDatabase::class.java.canonicalName,
    AppSqliteOpenHelperFactory()
) {

  fun createDatabase(version: Int): SupportSQLiteDatabase {
    val db = super.createDatabase(TEST_DB_NAME, version)
    db.setForeignKeyConstraintsEnabled(true)
    return db
  }

  fun migrateTo(version: Int, vararg migrations: Migration): SupportSQLiteDatabase {
    return super.runMigrationsAndValidate(TEST_DB_NAME, version, true, *migrations)
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
