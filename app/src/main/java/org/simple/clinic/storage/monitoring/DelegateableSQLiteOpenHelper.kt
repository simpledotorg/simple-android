package org.simple.clinic.storage.monitoring

import androidx.sqlite.db.SupportSQLiteOpenHelper

class DelegateableSQLiteOpenHelper private constructor(
    private val delegate: SQLiteDatabaseSqlDelegate,
    private val openHelper: SupportSQLiteOpenHelper
) : SupportSQLiteOpenHelper {

  override fun close() {
    openHelper.close()
  }

  override fun getDatabaseName() = openHelper.databaseName

  override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
    openHelper.setWriteAheadLoggingEnabled(enabled)
  }

  override fun getWritableDatabase() = DelegateableSQLiteDatabase(
      database = openHelper.writableDatabase,
      delegate = delegate
  )

  override fun getReadableDatabase() = DelegateableSQLiteDatabase(
      database = openHelper.readableDatabase,
      delegate = delegate
  )

  class Factory(
      private val delegate: SQLiteDatabaseSqlDelegate,
      private val factory: SupportSQLiteOpenHelper.Factory
  ) : SupportSQLiteOpenHelper.Factory {

    override fun create(
        configuration: SupportSQLiteOpenHelper.Configuration
    ) = DelegateableSQLiteOpenHelper(
        delegate = delegate,
        openHelper = factory.create(configuration)
    )
  }
}
