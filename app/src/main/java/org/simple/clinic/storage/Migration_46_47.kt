package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_46_47 : Migration(46, 47) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      DROP TABLE "HomescreenIllustration"
    """)
  }
}
