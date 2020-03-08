package org.simple.clinic.storage.migrations

import androidx.annotation.CallSuper
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.junit.Before
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.junit.rules.RuleChain
import org.simple.clinic.MigrationTestHelperWithForeignKeyConstraints
import org.simple.clinic.TestClinicApp
import org.simple.clinic.util.Rules
import javax.inject.Inject

abstract class BaseDatabaseMigrationTest(
    private val fromVersion: Int,
    private val toVersion: Int
) {

  private val helper = MigrationTestHelperWithForeignKeyConstraints()

  private val expectedException: ExpectedException = ExpectedException.none()

  @get:Rule
  val ruleChain: RuleChain = Rules
      .global()
      .around(expectedException)
      .around(helper)

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
