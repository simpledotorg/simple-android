package org.simple.clinic.storage

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import org.simple.clinic.patient.nameToSearchableForm

@Suppress("ClassName")
class Migration_4_5 : Migration(4, 5) {

  override fun migrate(database: SupportSQLiteDatabase) {
    // Update local searchable name in the Patient table to strip out the newly added characters
    database.inTransaction {
      compileStatement("""UPDATE "Patient" SET "searchableName"=? WHERE "uuid"=?""")
          .use { statement ->
            query("""SELECT "uuid","fullName" FROM "Patient"""")
                .use { cursor ->
                  val uuidIndex = cursor.getColumnIndex("uuid")
                  val fullNameIndex = cursor.getColumnIndex("fullName")

                  generateSequence { cursor.takeIf { it.moveToNext() } }
                      .map { it.getString(uuidIndex) to nameToSearchableForm(it.getString(fullNameIndex)) }
                      .forEach { (uuid, searchableName) ->
                        statement.bindString(1, searchableName)
                        statement.bindString(2, uuid)
                        statement.executeUpdateDelete()
                      }
                }
          }
    }
  }
}
