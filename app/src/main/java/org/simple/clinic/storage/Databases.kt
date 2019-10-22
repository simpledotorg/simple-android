package org.simple.clinic.storage

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * This extension function is to be used in Room Migrations only.
 * For running database queries in a transaction otherwise, use [RoomDatabase#runInTransaction(Runnable)]
 **/
fun SupportSQLiteDatabase.inTransaction(block: SupportSQLiteDatabase.() -> Unit) {
  beginTransaction()
  try {
    block.invoke(this)
    setTransactionSuccessful()
  } finally {
    endTransaction()
  }
}
