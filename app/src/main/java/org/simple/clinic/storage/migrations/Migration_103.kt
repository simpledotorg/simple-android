package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_103 @Inject constructor() : Migration(102, 103) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `PatientAddressFts` USING FTS4(`uuid` TEXT NOT NULL, `colonyOrVillage` TEXT, content=`PatientAddress`)")
      execSQL("INSERT INTO `PatientAddressFts`(`PatientAddressFts`) VALUES('rebuild')")
    }
  }
}
