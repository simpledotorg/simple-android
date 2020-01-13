package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import org.simple.clinic.storage.string
import javax.inject.Inject

@Suppress("ClassName")
class Migration_56_57 @Inject constructor() : Migration(56, 57) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      val patientUuidsWithBlankIdentifiers = findPatientUuidsWithBlankBusinessIds()

      if (patientUuidsWithBlankIdentifiers.isNotEmpty()) {
        val joinedPatientUuidsForInClause = patientUuidsWithBlankIdentifiers.joinToString(",") { "'$it'" }

        execSQL(""" 
        UPDATE "Patient" SET "syncStatus" = 'PENDING'
        WHERE "uuid" IN ($joinedPatientUuidsForInClause)
      """)
      }

      execSQL(""" DELETE FROM "BusinessId" WHERE "identifier" = '' """)
    }
  }

  private fun SupportSQLiteDatabase.findPatientUuidsWithBlankBusinessIds(): List<String> {
    return query(""" SELECT DISTINCT "patientUuid" FROM "BusinessId" WHERE "identifier" = '' """)
        .use { cursor ->
          generateSequence { cursor.takeIf { it.moveToNext() } }
              .map { it.string("patientUuid")!! }
              .toList()
        }
  }
}
