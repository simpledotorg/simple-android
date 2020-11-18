package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_84 @Inject constructor() : Migration(83, 84) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL(""" DELETE FROM "LoggedInUser" WHERE "loggedInStatus" = 'NOT_LOGGED_IN' """)
    }
  }
}
