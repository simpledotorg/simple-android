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

                        // This migration was edited after it went to production. The first version
                        // of the migration had a bug where BPs which were deleted would end up
                        // generating migrations where the deletedAt property was set to the string
                        // "null" instead of SQL NULL value.
                        //
                        // We fixed this migration, but some users did end up running this migration
                        // and would have encounters which should technically be deleted, but still
                        // show up in the UI because the deletedAt field is non-null.
                        //
                        // We need to add another migration to delete those encounters.
                        // TODO(vs): 2019-10-24 Add a migration for deleting the encounters with "null" deletedAt

                        val bpDeletedAt = cursorRow.string("deletedAt")

                        // Technically, we should generate an encounter with deletedAt != null for a
                        // deleted BP. However, these will never be show in the UI and the server
                        // will eventually send those encounters to us when we sync.
                        //
                        // For convenience, we can skip generating these encounters for us.
                        if (bpDeletedAt == null) {
                          execSQL("""
                            INSERT OR IGNORE INTO "Encounter" 
                            VALUES (
                              '$encounterId',
                              '$patientUuid',
                              '$encounteredOn',
                              '${cursorRow.string("createdAt")}',
                              '${cursorRow.string("updatedAt")}',
                              NULL
                            )
                            """
                          )
                        }

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
