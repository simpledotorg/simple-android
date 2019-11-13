package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

/**
 * Adds the column `loggedInStatus` to the `LoggedInUser` table
 **/
@Suppress("ClassName")
class Migration_7_8 @Inject constructor() : Migration(7, 8) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      database.execSQL("""ALTER TABLE "LoggedInUser" ADD COLUMN "loggedInStatus" TEXT NOT NULL DEFAULT ''""")
      database.execSQL("""UPDATE "LoggedInUser" SET "loggedInStatus" = 'LOGGED_IN'""")
    }
  }
}
