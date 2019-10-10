package org.simple.clinic.storage

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.ClinicApp
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.generateEncounterUuid
import org.simple.clinic.util.toLocalDateAtZone
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

@Suppress("ClassName")
class Migration_49_50 : Migration(49, 50) {

  @Inject
  lateinit var userClock: UserClock

  override fun migrate(database: SupportSQLiteDatabase) {
    ClinicApp.appComponent.inject(this)

    val tableName = "BloodPressureMeasurement"

    database.inTransaction {
      //Using a random UUID to give a default value to non-null encounterUuid temporarily.
      //The encounterUuid will be replaced with the correct value when the query executes in next line.
      database.execSQL(""" ALTER TABLE $tableName ADD COLUMN "encounterUuid" TEXT NOT NULL DEFAULT '05f9c798-1701-4379-b14a-b1b18d937a33'  """)
      database.compileStatement("""UPDATE $tableName SET "encounterUuid" = ? WHERE "uuid" = ?  """)
          .use { statement ->
            query(""" SELECT * FROM $tableName """)
                .use { cursor ->

                  generateSequence { cursor.takeIf { it.moveToNext() } }
                      .map { it.string("uuid") to it }
                      .forEach { (uuid, cursorRow) ->
                        val patientUuid = cursor.string("patientUuid")
                        val encounteredOn = Instant.parse(cursorRow.string("recordedAt")).toLocalDateAtZone(userClock.zone)
                        val encounterId = generateEncounterUuid(
                            facilityUuid = UUID.fromString(cursorRow.string("facilityUuid")),
                            patientUuid = UUID.fromString(patientUuid),
                            encounteredDate = encounteredOn
                        ).toString()

                        execSQL(""" INSERT OR IGNORE INTO "Encounter" VALUES (
                          '$encounterId',
                          '$patientUuid',
                          '$encounteredOn',
                          '${cursorRow.string("createdAt")}',
                          '${cursorRow.string("updatedAt")}',
                          '${cursorRow.string("deletedAt")}'
                          )""")

                        statement.bindString(1, encounterId)
                        statement.bindString(2, uuid)
                        statement.executeUpdateDelete()
                      }
                }
          }
    }
  }

  private fun Cursor.string(column: String): String? = getString(getColumnIndex(column))
}
