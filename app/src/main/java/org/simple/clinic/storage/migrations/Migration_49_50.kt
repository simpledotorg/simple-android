package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import org.simple.clinic.storage.string
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.generateEncounterUuid
import org.simple.clinic.util.room.InstantRoomTypeConverter
import org.simple.clinic.util.room.LocalDateRoomTypeConverter
import org.simple.clinic.util.toLocalDateAtZone
import java.util.UUID
import javax.inject.Inject

@Suppress("ClassName")
class Migration_49_50 @Inject constructor(
    val userClock: UserClock,
    val instantConverter: InstantRoomTypeConverter,
    val localDateConverter: LocalDateRoomTypeConverter
) : Migration(49, 50) {

  override fun migrate(database: SupportSQLiteDatabase) {
    val tableName = "BloodPressureMeasurement"

    database.inTransaction {

      //Using a random UUID to give a default value to non-null encounterUuid temporarily.
      //The encounterUuid will be replaced with the correct value when the query executes in next line.
      database.execSQL(""" ALTER TABLE $tableName ADD COLUMN "encounterUuid" TEXT NOT NULL DEFAULT '05f9c798-1701-4379-b14a-b1b18d937a33'  """)

      database.compileStatement("""INSERT OR IGNORE INTO "Encounter" VALUES (?, ?, ?, ?, ?, NULL)""")
          .use { insertIntoEncounter ->

            database.compileStatement("""UPDATE $tableName SET "encounterUuid" = ? WHERE "uuid" = ?  """)
                .use { updateBpWithEncounter ->

                  query(""" SELECT "uuid", "patientUuid", "facilityUuid", "createdAt", "updatedAt", "deletedAt", "recordedAt" FROM $tableName """)
                      .use { cursor ->

                        generateSequence { cursor.takeIf { it.moveToNext() } }
                            .map { it.string("uuid") to it }
                            .forEach { (uuid, cursorRow) ->
                              val patientUuid = cursor.string("patientUuid")

                              val bpRecordedAt = instantConverter.toInstant(cursorRow.string("recordedAt"))!!
                              val encounteredOn = bpRecordedAt.toLocalDateAtZone(userClock.zone)

                              val encounterId = generateEncounterUuid(
                                  facilityUuid = UUID.fromString(cursorRow.string("facilityUuid")),
                                  patientUuid = UUID.fromString(patientUuid),
                                  encounteredDate = encounteredOn
                              ).toString()

                              val bpDeletedAt = cursorRow.string("deletedAt")

                              // Technically, we should generate an encounter with deletedAt != null for a
                              // deleted BP. However, these will never be show in the UI and the server
                              // will eventually send those encounters to us when we sync.
                              //
                              // For convenience, we can skip generating these encounters for us.
                              if (bpDeletedAt == null) {
                                with(insertIntoEncounter) {
                                  bindString(1, encounterId)
                                  bindString(2, patientUuid)
                                  bindString(3, localDateConverter.fromLocalDate(encounteredOn))
                                  bindString(4, cursorRow.string("createdAt"))
                                  bindString(5, cursorRow.string("updatedAt"))

                                  executeInsert()
                                }
                              }

                              with(updateBpWithEncounter) {
                                bindString(1, encounterId)
                                bindString(2, uuid)
                                executeUpdateDelete()
                              }
                            }
                      }
                }

          }
    }
  }
}
