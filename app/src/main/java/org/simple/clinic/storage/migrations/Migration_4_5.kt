package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_4_5 @Inject constructor() : Migration(4, 5) {

  /**
   * [Regex] for stripping patient names and search queries of white spaces and punctuation
   *
   * Currently matches the following characters
   * - Any whitespace
   * - Comma, Hyphen, SemiColon, Colon, Underscore, Apostrophe, Period
   * */
  private val spacePunctuationRegex = Regex("[\\s;_\\-:,'\\\\.]")

  private fun nameToSearchableForm(string: String) = string.replace(spacePunctuationRegex, "")

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
