package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_107 @Inject constructor() : Migration(106, 107) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
       ALTER TABLE "Questionnaire"
       ADD COLUMN "deletedAt" TEXT DEFAULT NULL  
     """.trimIndent())
    }
  }
}
