package org.simple.clinic.storage

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase

fun SupportSQLiteDatabase.inTransaction(block: SupportSQLiteDatabase.() -> Unit) {
  beginTransaction()
  try {
    block.invoke(this)
    setTransactionSuccessful()
  } finally {
    endTransaction()
  }
}

fun Cursor.string(column: String): String? {
  val columnIndex = getColumnIndex(column)
  return if (columnIndex > -1) {
    getString(columnIndex)
  } else {
    null
  }
}
