package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_43_44 @Inject constructor() : Migration(43, 44) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.beginTransaction()

    database.execSQL("""
      ALTER TABLE "OngoingLoginEntry" RENAME TO "OngoingLoginEntry_v43"
    """)

    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "OngoingLoginEntry" (
        "uuid" TEXT NOT NULL, "phoneNumber" TEXT,
        "pin" TEXT, "fullName" TEXT, "pinDigest" TEXT,
        "registrationFacilityUuid" TEXT, "status" TEXT,
        "createdAt" TEXT, "updatedAt" TEXT, PRIMARY KEY("uuid")
        )
    """)

    database.execSQL("""
      INSERT INTO "OngoingLoginEntry" (
        "uuid", "phoneNumber", "pin",
        "fullName", "pinDigest", "registrationFacilityUuid",
        "status", "createdAt", "updatedAt"
      )
      SELECT U."uuid", U."phoneNumber", OLE."pin",
        U."fullName", U."pinDigest", UFM."facilityUuid",
        U."status", U."createdAt", U."updatedAt"
      FROM "OngoingLoginEntry_v43" OLE
      INNER JOIN "LoggedInUser" U ON U."uuid" = OLE."uuid"
      INNER JOIN "LoggedInUserFacilityMapping" UFM ON UFM."userUuid" = OLE."uuid"
      WHERE UFM."isCurrentFacility" = 1 AND U."loggedInStatus" = 'OTP_REQUESTED'
    """)

    /*
    * A user being 'NOT_LOGGED_IN' can happen in two cases:
    *
    * - The user began the registration api call, but it never happened (maybe because of network
    * issues) and quit the app and then never used it until this migration ran. In this scenario,
    * the app will move the user back to the Registration Phone Screen to restart the registration
    * flow.
    *
    * - The user was found on the server, but before the request OTP call completed, they quit the
    * app and never opened it again until the update ran. In this scenario, the app should restart
    * the login flow.
    *
    * Both of these cases are handled in TheActivityController#initialScreenKey() and we no longer
    * need a user who is in the NOT_LOGGED_IN state.
    **/
    database.execSQL("""
        DELETE FROM "LoggedInUser"
        WHERE "loggedInStatus" = 'NOT_LOGGED_IN'
    """)

    database.execSQL("""
      DROP TABLE "OngoingLoginEntry_v43"
    """)

    database.setTransactionSuccessful()
    database.endTransaction()
  }
}
