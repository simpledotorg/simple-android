package org.simple.clinic.storage

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

/**
 * Adds the column `loggedInStatus` to the `LoggedInUser` table
 **/
@Suppress("ClassName")
class Migration_7_8 @javax.inject.Inject constructor() : Migration(7, 8) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      database.execSQL("""ALTER TABLE "LoggedInUser" ADD COLUMN "loggedInStatus" TEXT NOT NULL DEFAULT ''""")
      database.execSQL("""UPDATE "LoggedInUser" SET "loggedInStatus" = 'LOGGED_IN'""")
    }
  }
}
