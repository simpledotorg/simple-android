package org.simple.clinic.storage.migrations

import androidx.annotation.CallSuper
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Before
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.simple.clinic.MigrationTestHelperWithForeignKeyConstraints
import org.simple.clinic.TestClinicApp
import javax.inject.Inject

abstract class BaseDatabaseMigrationTest(
    private val fromVersion: Int,
    private val toVersion: Int
) {

  @get:Rule
  val helper = MigrationTestHelperWithForeignKeyConstraints()

  @get:Rule
  val expectedException: ExpectedException = ExpectedException.none()

  @Inject
  lateinit var migrations: List<@JvmSuppressWildcards Migration>

  @Before
  @CallSuper
  open fun setUp() {
    TestClinicApp.appComponent().inject(this)
    helper.migrations = migrations
  }

  protected val before: SupportSQLiteDatabase by lazy(LazyThreadSafetyMode.NONE) { helper.createDatabase(fromVersion) }

  protected val after: SupportSQLiteDatabase by lazy(LazyThreadSafetyMode.NONE) { helper.migrateTo(toVersion) }
}
