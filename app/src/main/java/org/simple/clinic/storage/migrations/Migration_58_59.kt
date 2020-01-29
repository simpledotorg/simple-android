package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_58_59 @Inject constructor() : Migration(58, 59) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        UPDATE "Patient" SET "syncStatus" = 'PENDING' WHERE "uuid" IN (
          SELECT P.uuid FROM "Patient" P
          INNER JOIN (
            SELECT "uuid" FROM "PatientAddress" 
                WHERE LENGTH("zone") > 0
                OR LENGTH("streetAddress") > 0
          ) PA ON P.addressUuid = PA.uuid
        )
      """)
    }
  }
}
