package org.simple.clinic.storage

import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.util.createUuid5
import org.simple.clinic.util.toLocalDateAtZone
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset

@Suppress("ClassName")
class Migration_47_48 : Migration(47, 48) {

  override fun migrate(database: SupportSQLiteDatabase) {

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
                        val encounteredOn = Instant.parse(cursorRow.string("recordedAt")).toLocalDateAtZone(ZoneOffset.UTC)
                        val encounterName = cursorRow.string("facilityUuid") + patientUuid + encounteredOn
                        val encounterId = createUuid5(encounterName).toString()


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
