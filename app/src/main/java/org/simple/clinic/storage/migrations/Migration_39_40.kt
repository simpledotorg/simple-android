package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_39_40 @Inject constructor() : Migration(39, 40) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      UPDATE "MedicalHistory"
      SET 
        ${migrateClauseForColumn("diagnosedWithHypertension")},
        ${migrateClauseForColumn("isOnTreatmentForHypertension")},
        ${migrateClauseForColumn("hasHadHeartAttack")},
        ${migrateClauseForColumn("hasHadStroke")},
        ${migrateClauseForColumn("hasHadKidneyDisease")},
        ${migrateClauseForColumn("hasDiabetes")}
    """)
  }

  private fun migrateClauseForColumn(column: String): String {
    return """
      "$column" = (
          CASE
            WHEN "$column" == 'YES' THEN 'yes'
            WHEN "$column" == 'NO' THEN 'no'
            WHEN "$column" == 'UNKNOWN' THEN 'unknown'
          END
        )
    """
  }
}
