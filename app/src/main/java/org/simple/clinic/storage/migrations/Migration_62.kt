package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_62 @Inject constructor() : Migration(61, 62) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "LoggedInUser" RENAME TO "LoggedInUser_old"
      """)

      // We do not currently track the registration facility of the
      // user in any place, so we set both the current and registration
      // facility to the same one from the facility mapping table.
      // This is fine for now, because it's only a temporary value.
      // Since we fetch the user everytime the app starts, this value
      // will eventually get set with the actual registration facility.
      execSQL("""
        CREATE TABLE IF NOT EXISTS "LoggedInUser" (
            "uuid" TEXT NOT NULL, "fullName" TEXT NOT NULL, "phoneNumber" TEXT NOT NULL, 
            "pinDigest" TEXT NOT NULL, "status" TEXT NOT NULL, 
            "createdAt" TEXT NOT NULL, "updatedAt" TEXT NOT NULL, 
            "loggedInStatus" TEXT NOT NULL, 
            "registrationFacilityUuid" TEXT NOT NULL, "currentFacilityUuid" TEXT NOT NULL, 
            PRIMARY KEY("uuid"), 
            FOREIGN KEY("registrationFacilityUuid") REFERENCES "Facility"("uuid") ON UPDATE NO ACTION ON DELETE NO ACTION , 
            FOREIGN KEY("currentFacilityUuid") REFERENCES "Facility"("uuid") ON UPDATE NO ACTION ON DELETE NO ACTION)
      """)

      execSQL(""" CREATE INDEX "index_LoggedInUser_registrationFacilityUuid" ON "LoggedInUser" ("registrationFacilityUuid") """)
      execSQL(""" CREATE INDEX "index_LoggedInUser_currentFacilityUuid" ON "LoggedInUser" ("currentFacilityUuid") """)

      execSQL("""
        INSERT INTO "LoggedInUser" (
            "uuid", "fullName", "phoneNumber",
            "pinDigest", "status", "createdAt",
            "updatedAt", "loggedInStatus",
            "registrationFacilityUuid", "currentFacilityUuid"
        )
        SELECT U."uuid", U."fullName", U."phoneNumber",
            U."pinDigest", U."status", U."createdAt",
            U."updatedAt", U."loggedInStatus",
            FM."facilityUuid", FM."facilityUuid"
          FROM "LoggedInUser_old" U INNER JOIN "LoggedInUserFacilityMapping" FM ON U."uuid" = FM."userUuid"
          WHERE FM."isCurrentFacility" = 1
      """)

      execSQL(""" DROP TABLE "LoggedInUser_old" """)
      execSQL(""" DROP TABLE "LoggedInUserFacilityMapping" """)
    }
  }
}
