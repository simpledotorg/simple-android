package org.simple.clinic.storage

import android.arch.persistence.db.SupportSQLiteDatabase

fun SupportSQLiteDatabase.inTransaction(block: SupportSQLiteDatabase.() -> Unit) {
  beginTransaction()
  try {
    block.invoke(this)
    setTransactionSuccessful()
  } finally {
    endTransaction()
  }
}
