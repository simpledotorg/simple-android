package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("Classname")
class Migration_73 @Inject constructor() : Migration(72, 73) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""ALTER TABLE "PrescribedDrug" ADD COLUMN "frequency" TEXT DEFAULT NULL""")
      execSQL("""ALTER TABLE "PrescribedDrug" ADD COLUMN "durationInDays" INTEGER DEFAULT NULL""")
    }
  }
}
