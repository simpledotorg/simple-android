package org.simple.clinic.storage

import androidx.sqlite.db.SupportSQLiteOpenHelper
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppSqliteOpenHelperFactory
import org.simple.clinic.storage.monitoring.DelegateableSQLiteOpenHelper
import org.simple.clinic.storage.monitoring.FirebasePerfReportingSqlDelegate

@Module
class SqliteModule {

  @Provides
  fun sqliteOpenHelperFactory(
      sqlMonitoringDelegate: FirebasePerfReportingSqlDelegate
  ): SupportSQLiteOpenHelper.Factory {
    val factory = AppSqliteOpenHelperFactory()

    return DelegateableSQLiteOpenHelper.Factory(
        delegate = sqlMonitoringDelegate,
        factory = factory
    )
  }
}
