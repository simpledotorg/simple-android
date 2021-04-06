package org.simple.clinic.storage.monitoring

import android.content.ContentValues
import android.database.Cursor
import android.os.CancellationSignal
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery

interface SQLiteDatabaseSqlDelegate {

  fun query(
      database: SupportSQLiteDatabase,
      query: String?
  ): Cursor

  fun query(
      database: SupportSQLiteDatabase,
      query: String?,
      bindArgs: Array<out Any>?
  ): Cursor

  fun query(
      database: SupportSQLiteDatabase,
      query: SupportSQLiteQuery?
  ): Cursor

  fun query(
      database: SupportSQLiteDatabase,
      query: SupportSQLiteQuery?,
      cancellationSignal: CancellationSignal?
  ): Cursor

  fun insert(
      database: SupportSQLiteDatabase,
      table: String?,
      conflictAlgorithm: Int,
      values: ContentValues?
  ): Long

  fun delete(
      database: SupportSQLiteDatabase,
      table: String?,
      whereClause: String?,
      whereArgs: Array<out Any>?
  ): Int

  fun update(
      database: SupportSQLiteDatabase,
      table: String?,
      conflictAlgorithm: Int,
      values: ContentValues?,
      whereClause: String?,
      whereArgs: Array<out Any>?
  ): Int

  fun execSQL(
      database: SupportSQLiteDatabase,
      sql: String?
  )

  fun execSQL(
      database: SupportSQLiteDatabase,
      sql: String?,
      bindArgs: Array<out Any>?
  )
}
