package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_78 @Inject constructor() : Migration(77, 78) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL(""" ALTER TABLE "LoggedInUser" RENAME TO "LoggedInUser_Prev" """)

      execSQL("""
        CREATE TABLE IF NOT EXISTS "LoggedInUser" (
          "uuid" TEXT NOT NULL, 
          "fullName" TEXT NOT NULL, 
          "phoneNumber" TEXT NOT NULL, 
          "pinDigest" TEXT NOT NULL, 
          "status" TEXT NOT NULL, 
          "createdAt" TEXT NOT NULL, 
          "updatedAt" TEXT NOT NULL, 
          "loggedInStatus" TEXT NOT NULL, 
          "registrationFacilityUuid" TEXT NOT NULL, 
          "currentFacilityUuid" TEXT NOT NULL, 
          "teleconsultPhoneNumber" TEXT, 
          "capability_canTeleconsult" TEXT, 
          PRIMARY KEY("uuid")
        )
      """)

      execSQL("""
        INSERT INTO "LoggedInUser" (
          "uuid", 
          "fullName", 
          "phoneNumber", 
          "pinDigest", 
          "status", 
          "createdAt", 
          "updatedAt", 
          "loggedInStatus", 
          "registrationFacilityUuid", 
          "currentFacilityUuid", 
          "teleconsultPhoneNumber", 
          "capability_canTeleconsult"
        ) SELECT
          "uuid", 
          "fullName", 
          "phoneNumber", 
          "pinDigest", 
          "status", 
          "createdAt", 
          "updatedAt", 
          "loggedInStatus", 
          "registrationFacilityUuid", 
          "currentFacilityUuid", 
          "teleconsultPhoneNumber", 
          "capability_canTeleconsult"
        FROM "LoggedInUser_Prev"
      """)

      execSQL(""" DROP TABLE "LoggedInUser_Prev" """)
    }
  }
}
