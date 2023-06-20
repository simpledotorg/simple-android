package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_112 @Inject constructor() : Migration(111, 112) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("CREATE INDEX IF NOT EXISTS `index_CallResult_appointmentId` ON `CallResult` (`appointmentId`)")
  }
}
