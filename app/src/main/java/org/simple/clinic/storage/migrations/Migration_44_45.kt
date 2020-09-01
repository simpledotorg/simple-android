package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_44_45 @Inject constructor() : Migration(44, 45) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("PRAGMA foreign_keys = OFF")
    database.inTransaction {
      database.execSQL("PRAGMA legacy_alter_table = ON")

      database.execSQL("""
        DROP INDEX "index_Patient_addressUuid"
      """)
      database.execSQL("""
        ALTER TABLE "Patient" RENAME TO "Patient_v44"
      """)

      database.execSQL("""
        CREATE TABLE "Patient" (
          "uuid" TEXT NOT NULL, "addressUuid" TEXT NOT NULL, "fullName" TEXT NOT NULL, 
          "gender" TEXT NOT NULL, "dateOfBirth" TEXT, "status" TEXT NOT NULL,  
          "createdAt" TEXT NOT NULL, "updatedAt" TEXT NOT NULL, 
          "deletedAt" TEXT, "recordedAt" TEXT NOT NULL, 
          "syncStatus" TEXT NOT NULL, "age_value" INTEGER, "age_updatedAt" TEXT,
        PRIMARY KEY("uuid"),
        FOREIGN KEY("addressUuid") 
          REFERENCES "PatientAddress"("uuid")
          ON UPDATE CASCADE ON DELETE CASCADE
        )
      """)

      database.execSQL("""
        INSERT INTO "Patient" (
          "uuid", "addressUuid", "fullName", "gender",
          "dateOfBirth", "status", "createdAt", "updatedAt",
          "deletedAt", "recordedAt", "syncStatus", "age_value", "age_updatedAt"
        )
        SELECT "uuid", "addressUuid", "fullName", "gender",
          "dateOfBirth", "status", "createdAt", "updatedAt",
          "deletedAt", "recordedAt", "syncStatus", "age_value", "age_updatedAt"
        FROM "Patient_v44"
      """)

      database.execSQL("""
        CREATE INDEX "index_Patient_addressUuid" ON "Patient" ("addressUuid")
      """)

      database.execSQL("""
        DROP TABLE "Patient_v44"
      """)

      database.execSQL("PRAGMA legacy_alter_table = OFF")
      database.execSQL("PRAGMA foreign_key_check")
    }
    database.execSQL("PRAGMA foreign_keys = ON")
  }
}
