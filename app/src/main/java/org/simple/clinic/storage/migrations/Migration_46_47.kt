package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_46_47 @Inject constructor() : Migration(46, 47) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      DROP TABLE "HomescreenIllustration"
    """)
  }
}
