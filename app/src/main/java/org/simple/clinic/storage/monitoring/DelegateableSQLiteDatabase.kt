package org.simple.clinic.storage.monitoring

import android.content.ContentValues
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.util.Pair
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteStatement
import java.util.Locale

class DelegateableSQLiteDatabase(
    private val database: SupportSQLiteDatabase,
    private val delegate: SQLiteDatabaseSqlDelegate
) : SupportSQLiteDatabase {

  override fun close() {
    database.close()
  }

  override fun compileStatement(sql: String?): SupportSQLiteStatement = database.compileStatement(sql)

  override fun beginTransaction() {
    database.beginTransaction()
  }

  override fun beginTransactionNonExclusive() {
    database.beginTransactionNonExclusive()
  }

  override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener?) {
    database.beginTransactionWithListener(transactionListener)
  }

  override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener?) {
    database.beginTransactionWithListenerNonExclusive(transactionListener)
  }

  override fun endTransaction() {
    database.endTransaction()
  }

  override fun setTransactionSuccessful() {
    database.setTransactionSuccessful()
  }

  override fun inTransaction() = database.inTransaction()

  override fun isDbLockedByCurrentThread() = database.isDbLockedByCurrentThread

  override fun yieldIfContendedSafely() = database.yieldIfContendedSafely()

  override fun yieldIfContendedSafely(sleepAfterYieldDelay: Long) = database.yieldIfContendedSafely(sleepAfterYieldDelay)

  override fun getVersion() = database.version

  override fun setVersion(version: Int) {
    database.version = version
  }

  override fun getMaximumSize() = database.maximumSize

  override fun setMaximumSize(numBytes: Long) = database.setMaximumSize(numBytes)

  override fun getPageSize() = database.pageSize

  override fun setPageSize(numBytes: Long) {
    database.pageSize = numBytes
  }

  override fun query(query: String?) = delegate.query(database, query)

  override fun query(query: String?, bindArgs: Array<out Any>?) = delegate.query(database, query, bindArgs)

  override fun query(query: SupportSQLiteQuery?) = delegate.query(database, query)

  override fun query(query: SupportSQLiteQuery?, cancellationSignal: CancellationSignal?) =
      delegate.query(database, query, cancellationSignal)

  override fun insert(table: String?, conflictAlgorithm: Int, values: ContentValues?) =
      delegate.insert(database, table, conflictAlgorithm, values)

  override fun delete(table: String?, whereClause: String?, whereArgs: Array<out Any>?) =
      delegate.delete(database, table, whereClause, whereArgs)

  override fun update(table: String?, conflictAlgorithm: Int, values: ContentValues?, whereClause: String?, whereArgs: Array<out Any>?) =
      delegate.update(database, table, conflictAlgorithm, values, whereClause, whereArgs)

  override fun execSQL(sql: String?) {
    delegate.execSQL(database, sql)
  }

  override fun execSQL(sql: String?, bindArgs: Array<out Any>?) {
    delegate.execSQL(database, sql, bindArgs)
  }

  override fun isReadOnly() = database.isReadOnly

  override fun isOpen() = database.isOpen

  override fun needUpgrade(newVersion: Int) = database.needUpgrade(newVersion)

  override fun getPath(): String = database.path

  override fun setLocale(locale: Locale?) {
    database.setLocale(locale)
  }

  override fun setMaxSqlCacheSize(cacheSize: Int) {
    database.setMaxSqlCacheSize(cacheSize)
  }

  override fun setForeignKeyConstraintsEnabled(enable: Boolean) {
    database.setForeignKeyConstraintsEnabled(enable)
  }

  override fun enableWriteAheadLogging() = database.enableWriteAheadLogging()

  override fun disableWriteAheadLogging() {
    database.disableWriteAheadLogging()
  }

  override fun isWriteAheadLoggingEnabled() = database.isWriteAheadLoggingEnabled

  override fun getAttachedDbs(): MutableList<Pair<String, String>> = database.attachedDbs

  override fun isDatabaseIntegrityOk() = database.isDatabaseIntegrityOk
}
