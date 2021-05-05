package org.simple.clinic.storage

import androidx.sqlite.db.SupportSQLiteOpenHelper
import dagger.Module
import dagger.Provides
import org.simple.clinic.di.AppSqliteOpenHelperFactory
import org.simple.clinic.storage.monitoring.AnalyticsReportingSqlDelegate
import org.simple.clinic.storage.monitoring.DelegateableSQLiteOpenHelper

@Module
class SqliteModule {

  @Provides
  fun sqliteOpenHelperFactory(
      sqlMonitoringDelegate: AnalyticsReportingSqlDelegate
  ): SupportSQLiteOpenHelper.Factory {
    val factory = AppSqliteOpenHelperFactory()

    return DelegateableSQLiteOpenHelper.Factory(
        delegate = sqlMonitoringDelegate,
        factory = factory
    )
  }
}
